package com.wellnr.platform.core.services;

import com.google.common.collect.Lists;
import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.ReflectionUtils;
import com.wellnr.platform.common.exceptions.NotAuthorizedException;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.common.validation.ParameterName;
import com.wellnr.platform.common.validation.ValidationProxy;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.context.RootEntity;
import com.wellnr.platform.core.modules.users.values.users.User;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class ServiceImplementationProxy {

    private ServiceImplementationProxy() {

    }

    /**
     * Creates a service instance with auto-generated implementations and validations.
     *
     * @param context          The platform context instance.
     * @param serviceInterface The interface to implement.
     * @param <T>              The type of the service.
     * @return The service instance.
     */
    public static <T> T createService(PlatformContext context, Class<T> serviceInterface) {
        return createService(context, serviceInterface, null);
    }

    /**
     * Creates a service instance with auto-generated implementations and validations.
     *
     * @param context          The platform context instance.
     * @param serviceInterface The interface to implement.
     * @param delegate         An optional delegate if the service contains complex functions which should not be
     *                         auto-generated.
     * @param <T>              The type of the service.
     * @return The service instance.
     */
    public static <T> T createService(PlatformContext context, Class<T> serviceInterface, @Nullable T delegate) {
        var impl = createServiceImplementation(context, serviceInterface, delegate);
        return ValidationProxy.createProxy(impl, serviceInterface);
    }

    private static <T> T createServiceImplementation(PlatformContext context, Class<T> serviceInterface,
                                                     @Nullable T delegate) {
        /*
         * Validate class.
         */
        if (!serviceInterface.isInterface()) {
            throw new IllegalArgumentException(MessageFormat.format(
                "The supplied type `{0}` is no interface, but this is required to generate service implementation.",
                serviceInterface.getName()
            ));
        }

        /*
         * Generate method implementations.
         */
        var generatedMethods = ReflectionUtils
            .getMethods(serviceInterface, GeneratedImpl.class, null, null)
            .stream()
            .map(method -> Tuple.apply(method, createImplementation(context, serviceInterface, method)))
            .collect(Collectors.toMap(
                t -> getMethodIdentifier(t._1),
                Tuple2::get_2
            ));


        return ReflectionUtils.createProxy(
            serviceInterface, (proxy, method, args) -> {
                var identifier = getMethodIdentifier(method);

                if (generatedMethods.containsKey(identifier)) {
                    return generatedMethods.get(identifier).invoke(proxy, method, args);
                } else if (Objects.nonNull(delegate)) {
                    return method.invoke(delegate, args);
                } else if (method.isDefault()) {
                    return InvocationHandler.invokeDefault(proxy, method, args);
                } else {
                    throw new IllegalArgumentException(MessageFormat.format(
                        "Method `{0}#{1}` has not been generated, no delegate is available and its not a default " +
                            "implementation",
                        serviceInterface.getName(), method.getName()
                    ));
                }
            }
        );
    }

    private static String getMethodIdentifier(Method m) {
        return String.format(
            "%s(%s)",
            m.getName(),
            Arrays
                .stream(m.getParameters())
                .map(Parameter::getType)
                .map(Class::getName)
                .collect(Collectors.joining(", "))
        );
    }

    @SuppressWarnings({"unchecked", "SuspiciousInvocationHandlerImplementation"})
    private static InvocationHandler createImplementation(
        PlatformContext context,
        Class<?> serviceInterface,
        Method serviceMethod
    ) {
        /*
         * Check method constraints.
         */
        if (!(serviceMethod.getParameterCount() > 0 && serviceMethod.getParameters()[0].getType().equals(User.class))) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Service method `{0}#{1}` is not valid. Every service method must have `User executor` as first " +
                    "parameter.",
                serviceInterface.getName(), serviceMethod.getName()
            ));
        }

        /*
         * Find method of entities to which the call should be delegated.
         */
        var generatedImpl = serviceMethod.getAnnotation(GeneratedImpl.class);

        var matchingMethods = Arrays
            .stream(generatedImpl.delegate().getMethods())
            .filter(
                candidateMethod -> candidateMethod.getReturnType().equals(serviceMethod.getReturnType())
            )
            // Map candidate to optional of method + tuple of parameter assignments between service method and
            // delegate method.
            .<Optional<Tuple2<Method, List<Integer>>>>map(candidateMethod -> {
                /* Matching methods must have same name prefix.
                 * E.g, service name may be `deleteRegisteredUser`, the method name of RegisteredUser entity
                 * should be either `deleteRegisteredUser` or just `deleted`.
                 */
                if (!serviceMethod.getName().startsWith(candidateMethod.getName())) {
                    return Optional.empty();
                }

                return findMatchingParameters(
                    serviceMethod,
                    candidateMethod
                ).map(
                    integers -> Tuple2.apply(
                        candidateMethod,
                        integers
                    )
                );

            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        if (matchingMethods.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Cannot find delegate method from `{0}#{2}` within `{1}`.",
                serviceInterface.getName(), generatedImpl.delegate(), serviceMethod.getName()
            ));
        } else if (matchingMethods.size() > 1) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Found multiple possible delegate methods of `{0}#{2}` within `{1}`. Please ensure assignment is not " +
                    "ambiguous. Possible matches are: `{3}`.",
                serviceInterface.getName(), generatedImpl.delegate(), serviceMethod.getName(),
                matchingMethods.stream().map(t -> t._1.getName()).collect(Collectors.joining("`, `"))
            ));
        }

        /*
         * We found a match :)
         */
        var delegateMethod = matchingMethods.get(0);

        /*
         * If the method is annotated with `lookup` we need to find the correct method to find the specific entity.
         */
        if (generatedImpl.lookup().length > 0) {
            var lookupEntity = generatedImpl.lookup()[0];

            var longDelegateEntityName = generatedImpl.delegate().getSimpleName();
            var delegateEntityName = longDelegateEntityName.replaceAll("RootEntity", "");
            var lookUpMethodPrefix = ("get" + delegateEntityName).toLowerCase();

            var matchingLookupMethods = Arrays
                .stream(lookupEntity.getMethods())
                .<Optional<Tuple2<Method, List<Integer>>>>map(lookupMethod -> {
                    // Lookup methods must start with `get` followed by the entityName. RootEntity might be omitted.
                    if (!lookupMethod.getName().toLowerCase().startsWith(lookUpMethodPrefix)) {
                        return Optional.empty();
                    }

                    return findMatchingParameters(
                        serviceMethod,
                        lookupMethod
                    ).map(matchingParams -> Tuple2.apply(
                        lookupMethod,
                        matchingParams
                    ));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

            if (matchingLookupMethods.isEmpty()) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Cannot find lookup method from `{0}#{2}` within `{1}`.",
                    serviceInterface.getName(), lookupEntity.getName(), serviceMethod.getName()
                ));
            }

            if (matchingLookupMethods.size() > 1) {
                // There might be multiple matches based on data types.
                // In this case service method parameters require @ParameterName annotation.
                // Parameter names will be used to select correct method.

                matchingLookupMethods = matchingLookupMethods
                    .stream()
                    .filter(lookupMethod -> lookupMethod
                        .get_2()
                        .stream()
                        .map(idx -> serviceMethod.getParameters()[idx])
                        .allMatch(param -> {
                            var parameterName = param.getAnnotation(ParameterName.class);

                            if (Objects.isNull(parameterName)) {
                                return false;
                            }

                            return lookupMethod
                                .get_1()
                                .getName()
                                .toLowerCase()
                                .contains(parameterName.value().toLowerCase());
                        })
                    )
                    .collect(Collectors.toList());

                if (matchingLookupMethods.isEmpty()) {
                    throw new IllegalArgumentException(MessageFormat.format(
                        "Cannot find lookup method from `{0}#{2}` within `{1}`.",
                        serviceInterface.getName(), lookupEntity.getName(), serviceMethod.getName()
                    ));
                }

                if (matchingLookupMethods.size() > 1) {
                    throw new IllegalArgumentException(MessageFormat.format(
                        "Found multiple possible delegate methods of `{0}#{2}` within `{1}`. Please ensure assignment" +
                            " is not ambiguous. Possible matches are: `{3}`.",
                        serviceInterface.getName(), lookupEntity, serviceMethod.getName(),
                        matchingLookupMethods.stream().map(
                            t -> t._1.getName()).collect(Collectors.joining("`, `")
                        )
                    ));
                }
            }

            var matchingLookupMethod = matchingLookupMethods.get(0);

            if (matchingLookupMethod.get_1().getReturnType().isAssignableFrom(CompletionStage.class)) {
                return (proxy, m, args) -> {
                    var lookupEntityInstance = context.getOrCreateEntity(lookupEntity);

                    var lookupArgs = matchingLookupMethod
                        .get_2()
                        .stream()
                        .map(lookup_parameter_index -> args[lookup_parameter_index])
                        .toArray();

                    var delegateInstanceCS = ((CompletionStage<?>) matchingLookupMethod
                        .get_1()
                        .invoke(lookupEntityInstance, lookupArgs)
                    );

                    return delegateInstanceCS.thenCompose(delegateInstance -> {
                        if (delegateInstance.getClass().isAssignableFrom(generatedImpl.delegate())) {

                            checkPermission(
                                (User) args[0],
                                generatedImpl.permissions(),
                                (RootEntity) delegateInstance
                            );

                            var delegateParams = delegateMethod
                                .get_2()
                                .stream()
                                .map(service_parameters_index -> args[service_parameters_index])
                                .toArray();

                            var result = Operators.suppressExceptions(
                                () -> delegateMethod.get_1().invoke(delegateInstance, delegateParams)
                            );

                            if (result instanceof CompletionStage<?> cs) {
                                return (CompletionStage<Object>) cs;
                            } else {
                                return CompletableFuture.completedFuture(result);
                            }
                        } else {
                            throw new IllegalArgumentException(MessageFormat.format(
                                "Response type of `{0}#{1}` is not `{2}`",
                                lookupEntity,
                                matchingLookupMethod.get_1().getName(),
                                generatedImpl.delegate().getName()
                            ));
                        }
                    });
                };
            } else {
                return (proxy, m, args) -> {
                    var lookupEntityInstance = context.getOrCreateEntity(lookupEntity);

                    var lookupArgs = matchingLookupMethod
                        .get_2()
                        .stream()
                        .map(lookup_parameter_index -> args[lookup_parameter_index])
                        .toArray();

                    var delegateInstance = (RootEntity) matchingLookupMethod
                        .get_1()
                        .invoke(lookupEntityInstance, lookupArgs);

                    var delegateParams = delegateMethod
                        .get_2()
                        .stream()
                        .map(service_parameters_index -> args[service_parameters_index])
                        .toArray();

                    checkPermission(
                        (User) args[0],
                        generatedImpl.permissions(),
                        delegateInstance
                    );

                    return Operators.suppressExceptions(
                        () -> delegateMethod.get_1().invoke(delegateInstance, delegateParams)
                    );
                };
            }
        } else {
            /*
             * Method does not require a lookup.
             */

            return (proxy, m, args) -> {
                var delegateParams = delegateMethod
                    .get_2()
                    .stream()
                    .map(service_parameters_index -> args[service_parameters_index])
                    .toArray();

                var delegateInstance = context.getOrCreateEntity(generatedImpl.delegate());

                checkPermission(
                    (User) args[0],
                    generatedImpl.permissions(),
                    delegateInstance);

                return delegateMethod.get_1().invoke(delegateInstance, delegateParams);
            };
        }
    }

    /**
     * Checks whether the user has at least on of the permissions on one of the entities.
     *
     * @param permissions The list of permissions to check.
     * @param entity      The entity which is accessed (delegate) on which the access should be checked.
     */
    private static void checkPermission(User user, String[] permissions, RootEntity entity) {
        if (permissions.length > 0) {
            var allowed = Arrays
                .stream(permissions)
                .anyMatch(permission -> user.hasPermission(entity.getGUID(), permission));

            if (!allowed) {
                throw NotAuthorizedException.apply(
                    "You are not authorized to execute this action."
                );
            }
        }
    }

    private static Optional<List<Integer>> findMatchingParameters(Method caller, Method delegate) {
        /*
         * Matching methods must only contain parameters which are available within current method.
         * In the same order as mentioned in the interface.
         */
        if (caller.getParameters().length < delegate.getParameters().length) {
            return Optional.empty();
        }


        var service_param_index = 0;
        var matchedParams = Lists.<Integer>newArrayList();

        for (
            var delegate_param_index = 0;
            delegate_param_index < delegate.getParameterCount();
            delegate_param_index++) {

            var params_match = false;
            var delegateParam = delegate.getParameters()[delegate_param_index];

            while (!params_match && service_param_index < caller.getParameters().length) {
                var serviceParam = caller.getParameters()[service_param_index];

                if (delegateParam.getType().equals(serviceParam.getType())) {
                    matchedParams.add(service_param_index);
                    params_match = true;
                } else {
                    service_param_index = service_param_index + 1;
                }
            }

            if (!params_match) {
                return Optional.empty();
            }
        }

        return Optional.of(matchedParams);
    }

}
