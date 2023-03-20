package com.wellnr.platform.common.async;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.ReflectionUtils;
import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.common.tuples.Either;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class AsyncBoundaryProxy implements InvocationHandler {

    private final Object delegate;

    private final ExecutorService executors;

    private final ReadWriteLock stateLock;

    private State state;

    private static AsyncBoundaryProxy apply(Object delegate) {
        return apply(
            delegate,
            Executors.newFixedThreadPool(10),
            new ReentrantReadWriteLock(),
            State.apply(Mode.IDLE, new ConcurrentLinkedQueue<>(), 0)
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T delegate, Class<T> interfaceType) {
        return ReflectionUtils.createProxy(
            interfaceType, AsyncBoundaryProxy.apply(delegate)
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T delegate) {
        return createProxy(delegate, (Class<T>) delegate.getClass());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var maybeAsyncMethod = Optional.ofNullable(method.getAnnotation(AsyncMethod.class));

        if (maybeAsyncMethod.isEmpty()) {
            LoggerFactory.getLogger(delegate.getClass()).warn(
                "Method `{}` is not annotated with @AsyncMethod. Its strongly recommended to annotate " +
                    "methods of classes wrapped by {},",
                method.getName(), this.getClass().getSimpleName()
            );
        }


        var result = new CompletableFuture<>();
        this.addToInbox(Call.apply(
            method, args, result, maybeAsyncMethod.orElse(new AsyncMethod() {
                @Override
                public boolean pure() {
                    return false;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return AsyncMethod.class;
                }

            })
        ));

        if (method.getReturnType().isAssignableFrom(CompletionStage.class)) {
            return result;
        } else {
            try {
                return result.get();
            } catch (Throwable ex) {
                if (Objects.isNull(ex.getCause())) {
                    throw ex;
                } else {
                    throw ex.getCause();
                }
            }
        }
    }

    private void addToInbox(Call call) {
        this.updateState(currentState -> {
            currentState.inbox.add(call);
            return currentState;
        });

        this.process();
    }

    private void process() {
        this.updateState(currentState -> {
            if (currentState.getInbox().isEmpty()) {
                return currentState;
            }

            var nextCall = currentState.getInbox().element();

            if (
                nextCall.getMode().pure() && (currentState.getMode().equals(Mode.IDLE) || currentState.getMode()
                    .equals(Mode.PURE))
            ) {
                /*
                 * Pure methods can be executed along with others.
                 */
                var call = currentState.getInbox().poll();
                this.execute(call);

                return currentState
                    .withRunningCalls(currentState.runningCalls + 1)
                    .withMode(Mode.PURE);
            } else if (currentState.getMode().equals(Mode.IDLE)) {
                var call = currentState.getInbox().poll();
                this.execute(call);

                return currentState
                    .withRunningCalls(currentState.runningCalls + 1)
                    .withMode(Mode.WRITING);
            } else {
                return currentState;
            }
        });
    }

    private void execute(Call call) {
        CompletableFuture.runAsync(() -> {
            try {

                var result = call.method.invoke(delegate, call.args);

                if (!Objects.isNull(result) && result instanceof CompletionStage<?> cs) {
                    cs
                        .handle(Either::apply)
                        .thenApply(r -> r
                            .ifLeft(call.result::complete)
                            .ifRight(call.result::completeExceptionally));
                } else {
                    call.result.complete(result);
                }
            } catch (InvocationTargetException ex) {
                call.result.completeExceptionally(ex.getCause());
            } catch (Exception ex) {
                call.result.completeExceptionally(ex);
            }

            call
                .result
                .handle((i1, i2) -> Done.getInstance())
                .thenAccept(i -> {
                    this.updateState(s -> {
                        s = s.withRunningCalls(s.runningCalls - 1);

                        if (s.runningCalls <= 0) {
                            s = s.withMode(Mode.IDLE);
                        }

                        return s;
                    });

                    this.process();
                });
        }, this.executors);
    }

    private void updateState(Function1<State, State> updateFn) {
        this.stateLock.writeLock().lock();

        this.state = Operators.ignoreExceptionsWithDefault(
            () -> updateFn.get(this.state), state
        );

        this.stateLock.writeLock().unlock();
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class Call {

        Method method;

        Object[] args;

        CompletableFuture<Object> result;

        AsyncMethod mode;

    }

    @With
    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class State {

        Mode mode;

        Queue<Call> inbox;

        int runningCalls;

    }

    private enum Mode {
        /**
         * Nothing is currently executed.
         */
        IDLE,

        /**
         * A read operation is currently executing.
         * Additional read operations may be executed.
         * Writes are not allowed, to ensure consistent reads.
         */
        PURE,

        /**
         * A write operation is currently executing.
         * Additional operations must wait.
         */
        WRITING
    }

}
