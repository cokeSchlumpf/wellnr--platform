package com.wellnr.platform.common.tuples;

import com.wellnr.platform.common.functions.Function0;
import lombok.AllArgsConstructor;

/**
 * Helper class to implement functional if.
 */
@AllArgsConstructor(staticName = "apply")
public class When<T> {

    private final boolean condition;

    private final Function0<T> then;

    public T otherwise(Function0<T> otherwise) {
        if (condition) {
            return then.get();
        } else {
            return otherwise.get();
        }
    }

    @AllArgsConstructor(staticName = "apply")
    public static class IncompleteWhen {

        private final boolean condition;

        public <T> When<T> then(Function0<T> then) {
            return When.apply(condition, then);
        }

    }

}
