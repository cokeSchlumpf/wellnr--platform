package com.wellnr.platform.core.persistence.query;

import com.google.common.collect.Lists;
import com.wellnr.platform.common.Operators;
import com.wellnr.platform.core.persistence.Operations;
import com.wellnr.platform.core.persistence.query.annotations.CustomQuery;
import com.wellnr.platform.core.persistence.query.annotations.GUID;
import com.wellnr.platform.core.persistence.query.annotations.Path;
import com.wellnr.platform.core.persistence.query.filter.*;
import com.wellnr.platform.core.persistence.query.values.*;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;

public class Queries {

    private Queries() {

    }

    public static Field $(String selector) {
        return Field.apply(selector);
    }

    public static Select $(String selector, Value value) {
        return Select.apply(value, $(selector));
    }

    public static And and(Query... query) {
        return And.apply(Arrays.stream(query).toList());
    }

    public static ElemMatch match(Value selector, Filter query) {
        return ElemMatch.apply(selector, query);
    }

    public static Equals eq(Value value) {
        return Equals.apply(value);
    }

    public static Or or(Query... query) {
        return Or.apply(Arrays.stream(query).toList());
    }

    public static ParameterReference p(int index) {
        return ParameterReference.apply(index);
    }

    public static Uppercase uppercase(Value value) {
        return Uppercase.apply(value);
    }

    public static <T> StaticValue<T> v(T value) {
        return StaticValue.apply(value);
    }

    /**
     * Creates a query from the methods name.
     * <p>
     * ```
     * {QUERY_TYPE}{ENTITY_NAME}By{QUERY_STRING}
     * ```
     * <p>
     * Where
     * * `QUERY_TYPE` is `findAll|findOne|insertOrUpdate|remove`.
     * * `ENTITY_NAME` is the class name of the result type which should be returned.
     * * `QUERY_STRING` is the query string which is parsed by this method.
     * <p>
     * The `QUERY_STRING` is turned into the a {@link Query} following these rules:
     * <p>
     * ```
     * ${FIELD_NAME}((And|Or)${OTHER_FIELD_NAME})*
     * ```
     *
     * @param method The method which should be parsed.
     * @return The query parsed from the method.
     */
    public static Query fromMethod(Method method) {
        /*
         * Check if method has @Query annotation.
         */
        var maybeQuery = Optional.ofNullable(
            method.getAnnotation(CustomQuery.class)
        );

        if (maybeQuery.isPresent()) {
            var queryDefinitionClass = maybeQuery.get().value();
            var methodName = maybeQuery.get().methodName().length() > 0 ? maybeQuery.get()
                .methodName() : method.getName();
            var queryFactory = Operators.suppressExceptions(() -> queryDefinitionClass.getMethod(methodName));

            var query = Operators.suppressExceptions(() -> queryFactory.invoke(null));

            if (query instanceof Query q) {
                return q;
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                    "The method `{0}#{1}` does not return a Query.",
                    queryDefinitionClass.getName(), methodName
                ));
            }
        }

        /*
         * Generate queries for read/ delete methods.
         * Reading method pass only query parameters.
         */
        if (
            method.getName().startsWith(Operations.FIND_ALL) ||
                method.getName().startsWith(Operations.FIND_ONE) ||
                method.getName().startsWith(Operations.REMOVE)
        ) {
            return fromReadMethod(method);
        } else {
            return fromInsertMethod(method);
        }
    }

    private static Query fromInsertMethod(Method method) {
        if (method.getParameters().length != 1) {
            throw new IllegalArgumentException(MessageFormat.format(
                "The method `{0}` is no valid insertion method. Insertion methods must have exactly one parameter.",
                method.getName()
            ));
        }

        var maybeGUIDDefinition = Optional.ofNullable(method.getParameters()[0].getAnnotation(GUID.class));

        if (maybeGUIDDefinition.isPresent()) {
            var guidDef = maybeGUIDDefinition.get();

            if (guidDef.value().length == 0) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "The GUID annotation for parameter `{1}` of method `{0}` is no valid. There must be at least one " +
                        "path.",
                    method.getName(), method.getParameters()[0]
                ));
            }

            var values = Arrays.stream(guidDef.value()).map(path -> (Query) match(
                $(path.value()),
                eq($(path.value(), p(0)))
            )).toList();

            if (values.size() > 1) {
                return And.apply(values);
            } else {
                return values.get(0);
            }
        } else {
            /*
             * Try to derive an id automatically from the parameter type.
             */

            var maybeGUID = Arrays
                .stream(method.getParameters()[0].getType().getFields())
                .filter(m -> m.getName().equalsIgnoreCase("guid"))
                .findFirst();

            var maybeGUIDGetter = Arrays
                .stream(method.getParameters()[0].getType().getMethods())
                .filter(m -> m.getName().equalsIgnoreCase("getGUID"))
                .findFirst();

            if (maybeGUID.isEmpty() && maybeGUIDGetter.isEmpty()) {
                throw new IllegalArgumentException((MessageFormat.format(
                    "The entity does not contain a GUID field. Either add a GUID field to the entity `{0}` or " +
                        "manually specify a GUID using @GUID annotation.",
                    method.getParameters()[0].getType().getName()
                )));
            }

            return match(
                $("guid"),
                eq($("guid", p(0)))
            );
        }
    }

    private static Query fromReadMethod(Method method) {
        /*
         * Get `By` part of method.
         */
        var parts = method.getName().split("By");

        if (parts.length < 2) {
            // No By Part defined.
            return True.apply();
        }

        var queryParts = Arrays
            .stream(
                parts[1]
                    .replaceAll("And", " && ")
                    .replaceAll("Or", " || ")
                    .split(" ")
            )
            .toList();

        var currentOperation = "&&";
        var subQueries = Lists.<Query>newArrayList();
        var parametersCount = 0;

        for (var queryPart : queryParts) {
            if (queryPart.equals("&&") || queryPart.equals("||")) {
                if (!queryPart.equals(currentOperation) && subQueries.size() <= 1) {
                    currentOperation = queryPart;
                } else if (!queryPart.equals(currentOperation) && currentOperation.equals("&&")) {
                    var and = And.apply(subQueries);
                    subQueries.clear();
                    subQueries.add(and);
                    currentOperation = queryPart;
                } else if (!queryPart.equals(currentOperation)) { // && currentOperation.equals("||")
                    var or = Or.apply(subQueries);
                    subQueries.clear();
                    subQueries.add(or);
                    currentOperation = queryPart;
                }
            } else {
                if (method.getParameters().length < parametersCount) {
                    throw new IllegalArgumentException(MessageFormat.format(
                        "Method `{0}` is not compatible with the query defined from its name. The query expects at " +
                            "least `{1}` parameters.",
                        method.getName(), parametersCount
                    ));
                }

                var pathAnnotation = Optional.ofNullable(
                    method.getParameters()[parametersCount].getAnnotation(Path.class)
                );

                if (pathAnnotation.isPresent()) {
                    subQueries.add(match($(pathAnnotation.get().value()), eq(p(parametersCount))));
                } else {
                    subQueries.add(match($(queryPart), eq(p(parametersCount))));
                }

                parametersCount = parametersCount + 1;
            }
        }

        if (subQueries.size() > 1 && currentOperation.equals("&&")) {
            var and = And.apply(subQueries);
            subQueries.clear();
            subQueries.add(and);
        } else if (subQueries.size() > 1) { // && currentOperation.equals("||")
            var or = Or.apply(subQueries);
            subQueries.clear();
            subQueries.add(or);
        }

        /*
         * Check parameter count.
         */
        if (method.getParameters().length != parametersCount) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Method `{0}` is not compatible with the query defined from its name. The query expects `{1}` " +
                    "parameter(s), but the method has `{2}`.",
                method.getName(), parametersCount, method.getParameters().length
            ));
        }

        /*
         * Return final query.
         */
        if (subQueries.size() > 0) {
            return subQueries.get(0);
        } else {
            return True.apply();
        }
    }

}
