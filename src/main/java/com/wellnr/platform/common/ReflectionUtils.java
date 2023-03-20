package com.wellnr.platform.common;

import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.common.tuples.Tuple2;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.objenesis.ObjenesisStd;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    /**
     * Creates a proxy class for the provided type.
     *
     * @param type The type to proxy.
     * @param mh   The method handler to handle method calls.
     * @param <T>  The type to be proxy.
     * @return The proxy instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> type, MethodHandler mh) {
        if (type.isInterface()) {
            return (T) java.lang.reflect.Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[] { type },
                (proxy, method, args) -> mh.invoke(proxy, method, null, args)
            );
        } else {
            var factory = new ProxyFactory();
            factory.setSuperclass(type);

            var proxyClass = factory.createClass();
            var objenesis = new ObjenesisStd();
            var proxy = objenesis.newInstance(proxyClass);

            ((Proxy) proxy).setHandler(mh);
            return (T) proxy;
        }
    }

    /**
     * Creates a proxy class for the provided type.
     *
     * @param type The type to proxy.
     * @param ih   The method handler to handle method calls.
     * @param <T>  The type to proxy.
     * @return The proxy instance.
     */
    public static <T> T createProxy(Class<T> type, InvocationHandler ih) {
        return createProxy(type, (self, thisMethod, proceed, args) -> ih.invoke(self, thisMethod, args));
    }

    /**
     * Find a method by given criteria.
     *
     * @param type               The type to analyse.
     * @param annotation         Optional. An annotation class with which the method should be annotated.
     * @param expectedReturnType Optional. The expected return type of the method.
     * @param additionalCheck    Optional. An additional filter operation to select a method.
     * @return A method matching the criteria if found.
     */
    public static Optional<Method> getMethod(
        Class<?> type,
        @Nullable Class<? extends Annotation> annotation,
        @Nullable Class<?> expectedReturnType,
        @Nullable Function1<Method, Boolean> additionalCheck) {

        return getMethods(type, annotation, expectedReturnType, additionalCheck)
            .stream()
            .findFirst();
    }

    /**
     * Find a method by given criteria.
     *
     * @param type               The type to analyse.
     * @param annotation         Optional. An annotation class with which the method should be annotated.
     * @param expectedReturnType Optional. The expected return type of the method.
     * @param additionalCheck    Optional. An additional filter operation to select a method.
     * @return A method matching the criteria if found.
     */
    public static List<Method> getMethods(
        Class<?> type,
        @Nullable Class<? extends Annotation> annotation,
        @Nullable Class<?> expectedReturnType,
        @Nullable Function1<Method, Boolean> additionalCheck) {

        return Arrays
            .stream(type.getDeclaredMethods())
            .filter(m -> {
                if (Objects.nonNull(annotation)) {
                    return Objects.nonNull(m.getAnnotation(annotation));
                } else {
                    return true;
                }
            })
            .filter(m -> {
                if (Objects.nonNull(expectedReturnType)) {
                    return expectedReturnType.isAssignableFrom(m.getReturnType());
                } else {
                    return true;
                }
            })
            .filter(m -> {
                if (Objects.nonNull(additionalCheck)) {
                    return additionalCheck.get(m);
                } else {
                    return true;
                }
            })
            .toList();
    }

    /**
     * Checks field or related getter for existence of an annotation.
     *
     * @param type  The type to analyze.
     * @param field The field name.
     * @param <T>   The type of the annotation.
     * @return The annotation instance if found.
     */
    public static <T extends Annotation> Optional<T> getAnnotationForField(Class<?> type, String field,
                                                                           Class<T> annotationType) {
        var maybeFieldAnnotation = Arrays
            .stream(type.getDeclaredFields())
            .filter(f -> f.getName().equalsIgnoreCase(field))
            .findFirst()
            .flatMap(f -> Optional.ofNullable(f.getAnnotation(annotationType)));

        var maybeGetter = Arrays
            .stream(type.getMethods())
            .filter(m -> m.getName().equalsIgnoreCase("get" + field))
            .findFirst()
            .flatMap(m -> Optional.ofNullable(m.getAnnotation(annotationType)));

        if (maybeFieldAnnotation.isPresent()) {
            return maybeFieldAnnotation;
        } else {
            return maybeGetter;
        }
    }


    @SuppressWarnings("unchecked")
    public static <U> Function1<U, Object> getValueGetterForNestedFieldForClass(String field, Class<U> type) {
        var fields = Arrays.stream(field.split("\\.")).toList();
        var childField = Optional
            .of(String.join(".", fields.subList(1, fields.size())))
            .flatMap(s -> {
                if (s.length() > 0) {
                    return Optional.of(s);
                } else {
                    return Optional.empty();
                }
            });

        var objFieldTuple = getValueGetterForClass(fields.get(0), type);
        var objFieldGetter = objFieldTuple._1;
        var objFieldType = objFieldTuple._2;

        if (childField.isPresent()) {
            var nextFunction = getValueGetterForNestedFieldForClass(childField.get(), (Class<Object>) objFieldType);

            return obj -> {
                var nextObj = objFieldGetter.get(obj);

                if (Objects.nonNull(nextObj)) {
                    return nextFunction.apply(nextObj);
                } else {
                    return null;
                }
            };
        } else {
            return obj -> {
                var nextObj = objFieldGetter.get(obj);

                if (Objects.nonNull(nextObj)) {
                    return nextObj;
                } else {
                    return null;
                }
            };
        }
    }

    /**
     * Returns a function that reads a field from an object and returns its value, as well as the return type
     * of the function.
     *
     * @param field The field name (no nested path). Field must be direct field of `type`.
     * @param type  The type from which a field should be read.
     * @param <U>   The object type to read a value from.
     * @return A function to read a value from object of type U and the corresponding response type.
     */
    private static <U> Tuple2<Function1<U, Object>, Class<?>> getValueGetterForClass(String field, Class<U> type) {
        /*
         * Try to find getter.
         */
        var getter = Arrays
            .stream(type.getMethods())
            .filter(m -> m.getName().equalsIgnoreCase("get" + field))
            .filter(m -> m.getParameters().length == 0)
            .findFirst();

        var objField = Arrays
            .stream(type.getDeclaredFields())
            .filter(m -> m.getName().equalsIgnoreCase(field))
            .findFirst();

        if (getter.isPresent()) {
            return Tuple.apply(
                obj -> getter.get().invoke(obj),
                getter.get().getReturnType()
            );
        } else if (objField.isPresent()) {
            objField.get().setAccessible(true);

            return Tuple.apply(
                obj -> objField.get().get(obj),
                objField.get().getType()
            );
        } else {
            throw new RuntimeException(MessageFormat.format(
                "Can''t find field `{0}` within type `{1}`",
                field, type.getName()
            ));
        }
    }

}
