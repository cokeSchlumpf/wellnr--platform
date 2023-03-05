package com.wellnr.platform.common.functions;

import com.wellnr.platform.common.Operators;

@FunctionalInterface
public interface Procedure1<T> {

    void apply(T t) throws Exception;

    default void run(T t) {
        Operators.suppressExceptions(() -> this.apply(t));
    }

}
