package com.wellnr.platform.core.persistence.inmemory;

import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.tuples.Nothing;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.core.persistence.query.filter.*;
import com.wellnr.platform.core.persistence.query.values.*;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

@lombok.Value
@AllArgsConstructor(staticName = "apply")
public class QueryEngine<T> {

    Class<T> type;

    Collection<T> collection;

    Query query;

    List<Object> parameters;

    public List<T> findAll() {
        var condition = mapToCondition(query);

        return collection
            .stream()
            .filter(condition::get)
            .toList();
    }

    public Optional<T> findOne() {
        var condition = mapToCondition(query);

        return collection
            .stream()
            .filter(condition::get)
            .findFirst();
    }

    public void insertOrUpdate(T item) {
        var condition = mapToCondition(query);

        var newItems = Stream
            .concat(
                collection
                    .stream()
                    .filter(i -> !condition.get(i)),
                Stream.of(item)
            )
            .toList();

        collection.clear();
        collection.addAll(newItems);
    }

    public void remove() {
        var condition = mapToCondition(query);

        var newItems = collection
            .stream()
            .filter(i -> !condition.get(i))
            .toList();

        collection.clear();
        collection.addAll(newItems);
    }

    private Function1<T, Boolean> mapToCondition(Query query) {
        return mapToCondition(query, type);
    }

    private <U> Function1<U, Boolean> mapToCondition(Query query, Class<U> type) {
        if (query instanceof ElemMatch elemMatch) {
            return resolveElemMatch(elemMatch, type);
        } else if (query instanceof Equals equals) {
            return resolveEquals(equals, type);
        } else if (query instanceof And and) {
            return resolveAnd(and, type);
        } else if (query instanceof Or or) {
            return resolveOr(or, type);
        } else if (query instanceof True) {
            return obj -> true;
        } else if (query instanceof False) {
            return obj -> false;
        }

        throw new IllegalArgumentException(MessageFormat.format(
            "Can''t handle query type `{0}`",
            query.getClass().getName()
        ));
    }

    private <U> Function1<U, Boolean> resolveOr(Or or, Class<U> type) {
        var getters = or
            .getFilters()
            .stream()
            .map(query -> mapToCondition(query, type))
            .toList();

        return obj -> getters.stream().anyMatch(cond -> cond.get(obj));
    }

    private <U> Function1<U, Boolean> resolveAnd(And and, Class<U> type) {
        var getters = and.getFilters()
            .stream()
            .map(query -> mapToCondition(query, type))
            .toList();

        return obj -> getters.stream().allMatch(cond -> cond.get(obj));
    }

    private <U> Function1<U, Boolean> resolveEquals(Equals eq, Class<U> type) {
        var value = resolveValue(eq.getValue(), type);

        return obj -> obj.equals(value.apply(obj));
    }

    @SuppressWarnings("unchecked")
    private <U> Function1<U, Boolean> resolveElemMatch(ElemMatch elemMatch, Class<U> type) {
        return obj -> {
            var getLeft = resolveValue(elemMatch.getSelector(), (Class<Object>) obj.getClass());
            var left = getLeft.apply(obj);

            if (left instanceof Collection col) {
                if (col.isEmpty()) {
                    return false;
                }

                return col
                    .stream()
                    .anyMatch(item -> {
                        var getRight = mapToCondition(elemMatch.getQuery(), (Class<Object>) item.getClass());
                        return getRight.get(item);
                    });
            } else {
                var getRight = mapToCondition(elemMatch.getQuery(), (Class<Object>) obj.getClass());
                return getRight.apply(left);
            }
        };
    }

    private <U> Function1<U, Object> resolveValue(Value value, Class<U> type) {
        if (value instanceof Field f) {
            return resolveValueFromField(f, type);
        } else if (value instanceof ParameterReference ref) {
            return resolveValueFromParameterReference(ref);
        } else if (value instanceof StaticValue val) {
            return resolveValueFromStaticValue(val);
        } else if (value instanceof Uppercase upper) {
            return resolveValueFromUppercase(upper, type);
        } else if (value instanceof Select select) {
            return resolveValueFromSelect(select, type);
        }

        throw new IllegalArgumentException(MessageFormat.format(
            "Can''t handle value type `{0}`",
            value.getClass().getName()
        ));
    }

    @SuppressWarnings("unchecked")
    private <U> Function1<U, Object> resolveValueFromSelect(Select select, Class<U> type) {
        var getValue = resolveValue(select.getValue(), type);

        return obj -> {
            var value = getValue.apply(obj);

            if (Objects.isNull(value)) {
                return null;
            } else {
                var selectFunc = resolveValueFromField(select.getSelect(), (Class<Object>) value.getClass());
                return selectFunc.get(value);
            }
        };
    }

    private <U> Function1<U, Object> resolveValueFromUppercase(Uppercase upper, Class<U> type) {
        var getValue = resolveValue(upper.getValue(), type);
        return obj -> {
            var value = getValue.apply(obj);
            return value.toString().toUpperCase();
        };
    }

    private <U> Function1<U, Object> resolveValueFromStaticValue(StaticValue<?> val) {
        return obj -> val.getValue();
    }

    private <U> Function1<U, Object> resolveValueFromParameterReference(ParameterReference ref) {
        var result = this.parameters.get(ref.getIndex());
        return obj -> result;
    }

    @SuppressWarnings("unchecked")
    private <U> Function1<U, Object> resolveValueFromField(Field field, Class<U> type) {
        var objFieldTuple = getValueFromObject(field.getName(), type);
        var objFieldGetter = objFieldTuple._1;
        var objFieldType = objFieldTuple._2;

        var childField = field.getChildField();

        if (childField.isPresent()) {
            var nextFunction = resolveValueFromField(childField.get(), (Class<Object>) objFieldType);

            return obj -> {
                var nextObj = objFieldGetter.get(obj);

                if (Objects.nonNull(nextObj)) {
                    return nextFunction.apply(nextObj);
                } else {
                    return Nothing.getInstance();
                }
            };
        } else {
            return obj -> {
                var nextObj = objFieldGetter.get(obj);

                if (Objects.nonNull(nextObj)) {
                    return nextObj;
                } else {
                    return Nothing.getInstance();
                }
            };
        }
    }

    private <U> Tuple2<Function1<U, Object>, Class<?>> getValueFromObject(String field, Class<U> type) {
        /*
         * Try to find getter.
         */
        var getter = Arrays
            .stream(type.getMethods())
            .filter(m -> m.getName().toLowerCase().equals("get" + field.toLowerCase()))
            .filter(m -> m.getParameters().length == 0)
            .findFirst();

        var objField = Arrays
            .stream(type.getFields())
            .filter(m -> m.getName().toLowerCase().equals(field.toLowerCase()))
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
