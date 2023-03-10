package com.wellnr.platform.core.persistence.query;

import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.functions.Function2;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.Operations;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class QueryEngineRepository {

    @SuppressWarnings("unchecked")
    public static <R, T> R create(
        PlatformContext ctx, Class<R> repositoryType,
        Function1<Class<T>, QueryEngine<T>> engineFactory,
        List<Class<T>> entityTypes
    ) {
        var collections = entityTypes
            .stream()
            .collect(Collectors.toMap(Class::getName, engineFactory::get));

        var methods = Arrays
            .stream(repositoryType.getMethods())
            .filter(method ->
                Operations.ALL_OPERATIONS.stream().anyMatch(op -> method.getName().startsWith(op))
            )
            .filter(method -> !method.isDefault())
            .map(m ->
                Tuple.apply(m, createHandlerForMethod(m, entityTypes, collections))
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

    private static <T> InvocationHandler createHandlerForMethod(
        Method method, List<Class<T>> entityTypes,
        Map<String, QueryEngine<T>> collections) {

        for (var entityType : entityTypes) {
            var regex = MessageFormat.format(
                "^({0}){1}(s|es)?(By[a-zA-Z0-9]+)?$",
                String.join("|", Operations.ALL_OPERATIONS), entityType.getSimpleName()
            );

            var pattern = Pattern.compile(regex);
            var match = pattern.matcher(method.getName());

            if (match.matches()) {
                return createHandlerForMethod(method, entityType, collections);
            }
        }

        var regex = MessageFormat.format(
            "^({0}){1}(s|es)(By[a-zA-Z0-9]+)?$",
            String.join("|", Operations.ALL_OPERATIONS), entityTypes.get(0).getSimpleName()
        );

        throw new IllegalArgumentException(MessageFormat.format(
            "Can't detect entity for `{0}`. Please ensure to comply with Naming conventions. Expected pattern: `{1}`",
            method.getName(), regex
        ));
    }

    @SuppressWarnings({"unchecked", "SuspiciousInvocationHandlerImplementation"})
    private static <T> InvocationHandler createHandlerForMethod(
        Method method, Class<T> entityType, Map<String, QueryEngine<T>> collections
    ) {
        var query = Queries.fromMethod(method);

        Function2<QueryEngine<T>, List<Object>, Object> operation;

        if (method.getName().startsWith(Operations.FIND_ALL)) {
            operation = (engine, args) -> engine.findAll(query, args);
        } else if (method.getName().startsWith(Operations.FIND_ONE)) {
            operation = (engine, args) -> engine.findOne(query, args);
        } else if (method.getName().startsWith(Operations.UPSERT)) {
            operation = (engine, args) -> {
                engine.insertOrUpdate((T) args.get(0), query, args);
                return Done.getInstance();
            };
        } else if (method.getName().startsWith(Operations.REMOVE)) {
            operation = (engine, args) -> {
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

        return (proxy, ignore, args) -> {
            if (Objects.isNull(args)) {
                args = new Object[] {};
            }

            var engine = collections.get(entityType.getName());
            var result = operation.get(engine, Arrays.stream(args).toList());

            if (method.getReturnType().isAssignableFrom(CompletionStage.class)) {
                return CompletableFuture.completedFuture(result);
            } else {
                return result;
            }
        };
    }
}
