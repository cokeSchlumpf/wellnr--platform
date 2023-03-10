package com.wellnr.platform.core.persistence.inmemory;

import com.google.common.collect.Lists;
import com.wellnr.platform.common.ReflectionUtils;
import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.tuples.Nothing;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.core.persistence.query.QueryEngine;
import com.wellnr.platform.core.persistence.query.filter.*;
import com.wellnr.platform.core.persistence.query.values.*;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

@lombok.Value
@AllArgsConstructor(staticName = "apply")
public class InMemoryQueryEngine<T> implements QueryEngine<T> {

    Class<T> type;

    Collection<T> collection;

    public static <T> InMemoryQueryEngine<T> apply(Class<T> type) {
        return apply(type, Lists.newArrayList());
    }

    @Override
    public void insertOrUpdate(T item, Query match, List<Object> parameters) {
        var condition = mapToCondition(match, parameters);

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

    @Override
    public List<T> findAll(Query query, List<Object> parameters) {
        var condition = mapToCondition(query, parameters);

        return collection
            .stream()
            .filter(condition::get)
            .toList();
    }

    @Override
    public Optional<T> findOne(Query query, List<Object> parameters) {
        var condition = mapToCondition(query, parameters);

        return collection
            .stream()
            .filter(condition::get)
            .findFirst();
    }

    @Override
    public void remove(Query query, List<Object> parameters) {
        var condition = mapToCondition(query, parameters);

        var newItems = collection
            .stream()
            .filter(i -> !condition.get(i))
            .toList();

        collection.clear();
        collection.addAll(newItems);
    }

    private Function1<T, Boolean> mapToCondition(Query query, List<Object> parameters) {
        return mapToCondition(query, type, parameters);
    }

    private <U> Function1<U, Boolean> mapToCondition(Query query, Class<U> type, List<Object> parameters) {
        if (query instanceof ElemMatch elemMatch) {
            return resolveElemMatch(elemMatch, parameters);
        } else if (query instanceof Match match) {
            return resolveMatch(match, parameters);
        } else if (query instanceof Equals equals) {
            return resolveEquals(equals, type, parameters);
        } else if (query instanceof And and) {
            return resolveAnd(and, type, parameters);
        } else if (query instanceof Or or) {
            return resolveOr(or, type, parameters);
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

    private <U> Function1<U, Boolean> resolveOr(Or or, Class<U> type, List<Object> parameters) {
        var getters = or
            .getFilters()
            .stream()
            .map(query -> mapToCondition(query, type, parameters))
            .toList();

        return obj -> getters.stream().anyMatch(cond -> cond.get(obj));
    }

    private <U> Function1<U, Boolean> resolveAnd(And and, Class<U> type, List<Object> parameters) {
        var getters = and.getFilters()
            .stream()
            .map(query -> mapToCondition(query, type, parameters))
            .toList();

        return obj -> getters.stream().allMatch(cond -> cond.get(obj));
    }

    private <U> Function1<U, Boolean> resolveEquals(Equals eq, Class<U> type, List<Object> parameters) {
        var value = resolveValue(eq.getValue(), type, parameters);

        return obj -> obj.equals(value.apply(obj));
    }

    @SuppressWarnings("unchecked")
    private <U> Function1<U, Boolean> resolveMatch(Match elemMatch, List<Object> parameters) {
        return obj -> {
            var getLeft = resolveValue(elemMatch.getSelector(), (Class<Object>) obj.getClass(), parameters);
            var left = getLeft.apply(obj);
            var getRight = mapToCondition(elemMatch.getQuery(), (Class<Object>) obj.getClass(), parameters);
            return getRight.apply(left);
        };
    }

    @SuppressWarnings("unchecked")
    private <U> Function1<U, Boolean> resolveElemMatch(ElemMatch elemMatch, List<Object> parameters) {
        return obj -> {
            var getLeft = resolveValue(elemMatch.getSelector(), (Class<Object>) obj.getClass(), parameters);
            var left = getLeft.apply(obj);

            if (left instanceof Collection col) {
                if (col.isEmpty()) {
                    return false;
                }

                return col
                    .stream()
                    .anyMatch(item -> {
                        var getRight = mapToCondition(elemMatch.getQuery(), (Class<Object>) item.getClass(), parameters);
                        return getRight.get(item);
                    });
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                    "ElemMatch selector did not resolve to a collection. ElemMatch requires a collection to be " +
                        "selected." +
                        " The following ElemMatch query cannot be parsed: `{0}`",
                    elemMatch
                ));
            }
        };
    }

    private <U> Function1<U, Object> resolveValue(Value value, Class<U> type, List<Object> parameters) {
        if (value instanceof Field f) {
            return resolveValueFromField(f, type);
        } else if (value instanceof ParameterReference ref) {
            return resolveValueFromParameterReference(ref, parameters);
        } else if (value instanceof StaticValue val) {
            return resolveValueFromStaticValue(val);
        } else if (value instanceof Uppercase upper) {
            return resolveValueFromUppercase(upper, type, parameters);
        } else if (value instanceof Select select) {
            return resolveValueFromSelect(select, type, parameters);
        }

        throw new IllegalArgumentException(MessageFormat.format(
            "Can''t handle value type `{0}`",
            value.getClass().getName()
        ));
    }

    @SuppressWarnings("unchecked")
    private <U> Function1<U, Object> resolveValueFromSelect(Select select, Class<U> type, List<Object> parameters) {
        var getValue = resolveValue(select.getValue(), type, parameters);

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

    private <U> Function1<U, Object> resolveValueFromUppercase(Uppercase upper, Class<U> type,
                                                               List<Object> parameters) {
        var getValue = resolveValue(upper.getValue(), type, parameters);
        return obj -> {
            var value = getValue.apply(obj);
            return value.toString().toUpperCase();
        };
    }

    private <U> Function1<U, Object> resolveValueFromStaticValue(StaticValue<?> val) {
        return obj -> val.getValue();
    }

    private <U> Function1<U, Object> resolveValueFromParameterReference(ParameterReference ref,
                                                                        List<Object> parameters) {
        var result = parameters.get(ref.getIndex());
        return obj -> result;
    }

    private <U> Function1<U, Object> resolveValueFromField(Field field, Class<U> type) {
        return ReflectionUtils.getValueGetterForNestedFieldForClass(field.getFQN(), type);
    }

}
