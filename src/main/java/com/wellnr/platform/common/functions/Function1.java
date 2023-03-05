package com.wellnr.platform.common.functions;

import com.wellnr.platform.common.Operators;

@FunctionalInterface
public interface Function1<T, R> {

    R apply(T t) throws Exception;

    default R get(T t) {
        return Operators.suppressExceptions(() -> this.apply(t));
    }

}
