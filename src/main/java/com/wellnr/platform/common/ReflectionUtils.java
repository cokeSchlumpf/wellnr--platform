package com.wellnr.platform.common;

import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.common.tuples.Tuple2;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    /**
     * Checks field or related getter for existence of an annotation.
     *
     * @param type The type to analyze.
     * @param field The field name.
     * @return The annotation instance if found.
     * @param <T> The type of the annotation.
     */
    public static <T extends Annotation> Optional<T> getAnnotationForField(Class<?> type, String field, Class<T> annotationType) {
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
     * @param type The type from which a field should be read.
     * @param <U> The object type to read a value from.
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
