package com.wellnr.platform.core.services;

import com.google.common.collect.Lists;
import com.wellnr.platform.common.ReflectionUtils;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.common.validation.ParameterName;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.context.RootEntity;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServiceImplementationProxy {

    private ServiceImplementationProxy() {

    }

    public static <T> T createService(Class<T> serviceInterface, @Nullable T delegate) {
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
        var methodsToGenerate = ReflectionUtils.getMethods(serviceInterface, GeneratedImpl.class, null, null);

        return null;
    }

    private static InvocationHandler createImplementation(
        PlatformContext context,
        Class<?> serviceInterface,
        Method serviceMethod,
        List<Class<RootEntity>> entityTypes) {
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

            return (proxy, m, args) -> {
                return null;
            };
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

                return delegateMethod.get_1().invoke(delegateInstance, delegateParams);
            };
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
