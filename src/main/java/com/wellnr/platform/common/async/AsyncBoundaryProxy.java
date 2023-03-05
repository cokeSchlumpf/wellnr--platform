package com.wellnr.platform.common.async;

import com.wellnr.platform.common.tuples.Either;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class AsyncBoundaryProxy implements InvocationHandler {

    private final Object delegate;

    private final AtomicReference<State> state;

    private final ExecutorService executors;

    private static AsyncBoundaryProxy apply(Object delegate) {
        return apply(
            delegate,
            new AtomicReference<>(State.apply(false, new ConcurrentLinkedDeque<>())),
            Executors.newFixedThreadPool(10));
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T delegate, Class<T> interfaceType) {
        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class[]{interfaceType},
            AsyncBoundaryProxy.apply(delegate));
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T delegate) {
        return createProxy(delegate, (Class<T>) delegate.getClass().getInterfaces()[0]);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var isReadsOnly = !Objects.isNull(method.getAnnotation(ReadsOnly.class));
        var isWrites = !Objects.isNull(method.getAnnotation(Writes.class));

        if (!isReadsOnly && !isWrites) {
            LoggerFactory.getLogger(delegate.getClass())
                .warn("Method `{}` is not annotated with @ReadsOnly or @Writes. Its strongly recommended to annotate " +
                    "methods of classes wrapped by {}", method.getName(), this.getClass()
                    .getSimpleName());
        }

        if (isReadsOnly) {
            return method.invoke(delegate, args);
        } else {
            var result = new CompletableFuture<>();
            this.addToInbox(Call.apply(method, args, result));

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
    }

    private void addToInbox(Call call) {
        var previousState = this.state.getAndUpdate(currentState -> {
            if (currentState.running) {
                currentState.inbox.add(call);
            }

            return currentState.withRunning(true);
        });

        if (!previousState.isRunning()) {
            this.executors.execute(() -> this.execute(call));
        }
    }

    private void execute(Call call) {
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

        call.result.thenApply(obj -> {
            if (Objects.isNull(obj)) {
                return "null";
            } else {
                return obj;
            }
        }).handle(Either::apply).thenAccept(i -> {
            var updatedState = this.state.updateAndGet(currentState -> {
                if (currentState.inbox.isEmpty()) {
                    return currentState.withRunning(false);
                } else {
                    return currentState;
                }
            });

            if (updatedState.isRunning()) {
                var nextCall = updatedState.inbox.poll();
                this.executors.execute(() -> this.execute(nextCall));
            }
        });
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class Call {

        Method method;

        Object[] args;

        CompletableFuture<Object> result;

    }

    @With
    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class State {

        boolean running;

        Queue<Call> inbox;

    }

}
