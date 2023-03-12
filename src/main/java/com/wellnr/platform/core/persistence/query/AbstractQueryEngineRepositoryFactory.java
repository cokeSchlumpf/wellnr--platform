package com.wellnr.platform.core.persistence.query;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.memento.HasMemento;
import com.wellnr.platform.core.persistence.Operations;
import com.wellnr.platform.core.persistence.memento.Mementos;
import com.wellnr.platform.core.persistence.query.annotations.Entity;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Abstract factory class to create repositories from query engines.
 *
 * @param <T> The minimum type requirements for entities which want to use the query engine.
 * @param <C> The custom query type, if supported by the query engine.
 */
@AllArgsConstructor()
public abstract class AbstractQueryEngineRepositoryFactory<T, C> {

    private final PlatformContext ctx;

    @SuppressWarnings("unchecked")
    public <R> R create(
        PlatformContext ctx,
        Class<R> repositoryType,
        List<Class<T>> entityTypes
    ) {
        var collections = entityTypes
            .stream()
            .map(entityType -> {
                if (HasMemento.class.isAssignableFrom(entityType)) {
                    var getMementoFunc = Operators.suppressExceptions(
                        () -> entityType.getMethod("getMemento")
                    );

                    return Tuple.apply(
                        entityType,
                        createQueryEngine(
                            entityType, (Class<T>) getMementoFunc.getReturnType()
                        )
                    );
                } else {
                    return Tuple.apply(
                        entityType,
                        createQueryEngine(entityType, entityType));
                }
            })
            .collect(Collectors.toMap(Tuple2::get_1, Tuple2::get_2));

        var methods = Arrays
            .stream(repositoryType.getMethods())
            .filter(method ->
                Operations.ALL_OPERATIONS.stream().anyMatch(op -> method.getName().startsWith(op))
            )
            .filter(method -> !method.isDefault())
            .map(m ->
                Tuple.apply(m, createHandlerForMethod(repositoryType, m, entityTypes, collections))
            )
            .collect(Collectors.toMap(t -> t._1.getName(), t -> t._2));

        return (R) Proxy.newProxyInstance(
            repositoryType.getClassLoader(),
            new Class[]{repositoryType},
            (proxy, method, args) -> {
                if (methods.containsKey(method.getName())) {
                    return methods.get(method.getName()).invoke(proxy, method, args);
                } else if (method.isDefault()) {
                    return InvocationHandler.invokeDefault(proxy, method, args);
                } else {
                    throw new NotImplementedException(MessageFormat.format(
                        "The method `{0}` of `{1}` is not implemented automatically.",
                        method.getName(), repositoryType.getName()
                    ));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private InvocationHandler createHandlerForMethod(
        Class<?> repositoryType,
        Method method,
        List<Class<T>> entityTypes,
        Map<Class<T>, QueryEngine<T, C>> collections
    ) {
        /*
         * If a method is annotated with `@Entity` it will use the information
         * from the annotation to find the corresponding entity type.
         */
        var maybeEntityAnnotation = Optional.ofNullable(method.getAnnotation(Entity.class));

        if (maybeEntityAnnotation.isPresent()) {
            var entityType = maybeEntityAnnotation.get().value();

            if (!collections.containsKey(entityType)) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Method `{0}#{1}` references entity type `{2}`, but this type is not registered for this repository.",
                    repositoryType.getName(), method.getName(), entityType.getName()
                ));
            }

            return createHandlerForMethod(method, (Class<T>) entityType, collections);
        }

        /*
         * If method is not annotated, we search for the matching entity by name.
         */
        for (var entityType : entityTypes) {
            var entityName = entityType.getSimpleName().replaceAll("y$","i");
            var methodName = method.getName().replaceAll("y$", "i");

            var regex = MessageFormat.format(
                "^({0}){1}(s|es)?(By[a-zA-Z0-9]+)?$",
                String.join("|", Operations.ALL_OPERATIONS), entityName
            );

            var pattern = Pattern.compile(regex);
            var match = pattern.matcher(methodName);

            if (match.matches()) {
                return createHandlerForMethod(method, entityType, collections);
            }
        }

        var regex = MessageFormat.format(
            "^({0}){1}(s|es)(By[a-zA-Z0-9]+)?$",
            String.join("|", Operations.ALL_OPERATIONS), entityTypes.get(0).getSimpleName()
        );

        throw new IllegalArgumentException(MessageFormat.format(
            "Can''t detect entity for `{0}`. Please ensure to comply with Naming conventions. Expected pattern: `{1}`",
            method.getName(), regex
        ));
    }

    @SuppressWarnings({"unchecked", "SuspiciousInvocationHandlerImplementation"})
    private InvocationHandler createHandlerForMethod(
        Method method,
        Class<T> entityType,
        Map<Class<T>, QueryEngine<T, C>> collections
    ) {
        Function1<List<Object>, Object> operation;
        var engine = collections.get(entityType);
        var maybeCustomQuery = this.getCustomQueryFromMethod(method);

        if (maybeCustomQuery.isPresent()) {
            if (method.getName().startsWith(Operations.FIND_ALL)) {
                operation = (args) -> engine.findAll(maybeCustomQuery.get().get(args));
            } else if (method.getName().startsWith(Operations.FIND_ONE)) {
                operation = (args) -> engine.findOne(maybeCustomQuery.get().get(args));
            } else if (method.getName().startsWith(Operations.UPSERT)) {
                operation = (args) -> {
                    engine.insertOrUpdate((T) args.get(0), maybeCustomQuery.get().get(args));
                    return Done.getInstance();
                };
            } else if (method.getName().startsWith(Operations.REMOVE)) {
                operation = (args) -> {
                    engine.remove(maybeCustomQuery.get().get(args));
                    return Done.getInstance();
                };
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Can''t detect operation for method `{0}`. Please ensure to comply with Naming conventions." +
                        " Methods must start with `findAll`, `findOne`, `upsert` or `remove`.",
                    method.getName()
                ));
            }
        } else {
            var query = Queries.fromMethod(method);

            if (method.getName().startsWith(Operations.FIND_ALL)) {
                operation = (args) -> engine.findAll(query, args);
            } else if (method.getName().startsWith(Operations.FIND_ONE)) {
                operation = (args) -> engine.findOne(query, args);
            } else if (method.getName().startsWith(Operations.UPSERT)) {
                operation = (args) -> {
                    if (args.get(0) instanceof HasMemento<?> hasMemento) {
                        var memento = hasMemento.getMemento();
                        engine.insertOrUpdate((T) memento, query, args);
                    } else {
                        engine.insertOrUpdate((T) args.get(0), query, args);
                    }

                    return Done.getInstance();
                };
            } else if (method.getName().startsWith(Operations.REMOVE)) {
                operation = (args) -> {
                    engine.remove(query, args);
                    return Done.getInstance();
                };
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Can''t detect operation for method `{0}`. Please ensure to comply with Naming conventions." +
                        " Methods must start with `findAll`, `findOne`, `upsert` or `remove`.",
                    method.getName()
                ));
            }
        }

        /*
         * If entity type is `HasMemento` we need to resolve the memento to the actual entity type.
         */
        if (HasMemento.class.isAssignableFrom(entityType)) {
            var operationFinal = operation;

            var createFromMemento = Mementos.createFromMementoFunc(
                (Class<HasMemento<?>>) entityType
            );

            Function1<Object, CompletionStage<?>> transformResult = CompletableFuture::completedFuture;

            if (method.getName().startsWith(Operations.FIND_ALL)) {
                // Method must return a list.
                transformResult = r -> {
                    if (r instanceof List<?> l) {
                        var mapped = l
                            .stream()
                            .map(memento -> createFromMemento.get(ctx, memento));

                        return Operators.allOf(mapped);
                    } else {
                        throw new IllegalArgumentException(MessageFormat.format(
                            "`{0}`-Methods must return `List<T>` or `CompletionStage<List<T>>`, but `{1}` does not.",
                            Operations.FIND_ALL, method.getName()
                        ));
                    }
                };
            } else if (method.getName().startsWith(Operations.FIND_ONE)) {
                transformResult = r -> {
                    if (r instanceof Optional<?> opt) {
                        var mapped = opt.map(memento -> createFromMemento.get(ctx, memento));
                        return Operators.optCS(mapped);
                    } else {
                        throw new IllegalArgumentException(MessageFormat.format(
                            "`{0}`-Methods must return `Optional<T>` or `CompletionStage<Optional<T>>`, but `{1}` does not.",
                            Operations.FIND_ONE, method.getName()
                        ));
                    }
                };
            }

            if (
                method.getName().startsWith(Operations.FIND_ALL) ||
                    method.getName().startsWith(Operations.FIND_ONE)
            ) {
                var transformResultFinal = transformResult;
                operation = (args) -> {
                    var result = operationFinal.get(args);

                    if (result instanceof CompletionStage<?> resultCS) {
                        return resultCS.thenCompose(transformResultFinal::get);
                    } else {
                        return transformResultFinal.get(result);
                    }
                };
            }
        }

        if (HasMemento.class.isAssignableFrom(method.getReturnType())) {
            var operationFinal = operation;
            var createFromMemento = Mementos.createFromMementoFunc(
                (Class<HasMemento<?>>) method.getReturnType()
            );

            operation = (args) -> {
                var result = operationFinal.get(args);

                if (result instanceof CompletionStage<?> resultCS) {
                    return resultCS.thenCompose(r -> createFromMemento.get(this.ctx, r));
                } else {
                    return createFromMemento.get(this.ctx, result);
                }
            };
        }


        /*
         * The actual invocation handler uses the prepared operation.
         * Only the result type is matched/ transformed during each execution.
         */
        var operationFinal = operation;

        return (proxy, ignore, args) -> {
            if (Objects.isNull(args)) {
                args = new Object[]{};
            }

            var result = operationFinal.get(Arrays.stream(args).toList());

            /*
             * Match the result type to the method return type (CompletionStage vs. immediate response).
             */
            if ((result instanceof CompletionStage<?> resultCS) && method.getReturnType().isAssignableFrom(CompletionStage.class)) {
                return resultCS;
            } else if (result instanceof CompletionStage<?> resultCS) {
                return resultCS.toCompletableFuture().get();
            } else if (method.getReturnType().isAssignableFrom(CompletionStage.class)) {
                return CompletableFuture.completedFuture(result);
            } else {
                return result;
            }
        };
    }

    /**
     * This method is called to create an instance of the query engine which is used to store/ query entities
     * for a specific types managed by the repository.
     * <p>
     * The repository must be able to handle `mementoType`.
     *
     * @param entityType  The type of the entity managed by the repository.
     * @param mementoType If `entityType` implements `HasMemento`, this will be the memento type. If not,
     *                    it's the same type as entityType.
     * @return A {@link QueryEngine} instance which can be used to query entities.
     */
    protected abstract QueryEngine<T, C> createQueryEngine(Class<T> entityType, Class<T> mementoType);

    /**
     * This method might be overwritten by children,  if a repository should support custom queries.
     * <p>
     * The method is called during the process to generate queries from method names. If this method returns
     * a custom query, it will stop to infer the query.
     *
     * @param m The method to be analyzed.
     * @return A function which resolves to a custom query. The parameter is the parameter list of the actual
     * repository function.
     */
    @SuppressWarnings("unused")
    protected Optional<Function1<List<Object>, C>> getCustomQueryFromMethod(Method m) {
        return Optional.empty();
    }

}
