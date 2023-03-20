package com.wellnr.platform.common.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wellnr.platform.common.ReflectionUtils;
import com.wellnr.platform.common.functions.Function0;
import com.wellnr.platform.common.tuples.Tuple2;
import jakarta.validation.Validation;
import jakarta.validation.executable.ExecutableValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE, staticName = "apply")
public class ValidationProxy implements InvocationHandler {

    private final Object delegate;

    public static <T> T createProxy(T delegate, Class<T> type) {
        return ReflectionUtils.createProxy(
            type, ValidationProxy.apply(delegate)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try (
            var factory = Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
        ) {
            ExecutableValidator executableValidator = factory.getValidator().forExecutables();
            var result = executableValidator.validateParameters(proxy, method, args);


            if (!result.isEmpty()) {
                var parameterNames = this.getParameterNames(method);

                var messages = result
                    .stream()
                    .map(violation -> {
                        var path = replaceParameterNames(violation.getPropertyPath().toString(), method, parameterNames);
                        var message = violation.getMessage();

                        return String.format("%s: %s", path, message);
                    })
                    .collect(Collectors.toList());

                throw ValidationException.apply(
                    "Input parameters are not valid", messages
                );
            } else {
                return method.invoke(delegate, args);
            }
        }
    }

    private String replaceParameterNames(String path, Method method, Map<String, String> parameterNames) {
        for (var key : parameterNames.keySet()) {
            path = path.replaceFirst(key, parameterNames.get(key));
        }

        path = path.replaceFirst(method.getName() + "\\.", "");

        return path;
    }

    private Map<String, String> getParameterNames(Method method) {
        return Arrays
            .stream(method.getParameters())
            .map(p -> Stream
                .<Function0<Optional<String>>>of(
                    () -> Optional.ofNullable(p.getAnnotation(ParameterName.class)).map(ParameterName::value),
                    () -> Optional.ofNullable(p.getAnnotation(JsonProperty.class)).map(JsonProperty::value)
                )
                .map(Function0::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(name -> Tuple2.apply(p.getName(), name))
                .findFirst()
                .orElse(Tuple2.apply(p.getName(), p.getName())))
            .collect(Collectors.toMap(
                t -> t._1,
                t -> t._2
            ));
    }
}
