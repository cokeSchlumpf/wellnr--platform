package com.wellnr.platform.core.persistence.query.values;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A field selector to access a field within an object.
 */
@lombok.Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Field implements Value {

    List<String> elements;

    public static Field apply(List<String> elements) {
        return new Field(List.copyOf(elements));
    }

    public static Field apply(String selector) {
        var elements = Arrays.stream(selector.split("\\.")).toList();
        return apply(elements);
    }

    public String getName() {
        return elements.get(0);
    }

    public String getFQN() {
        return String.join(".", elements);
    }

    public Optional<Field> getChildField() {
        if (elements.size() <= 1) {
            return Optional.empty();
        } else {
            return Optional.of(apply(elements.subList(1, elements.size())));
        }
    }

}
