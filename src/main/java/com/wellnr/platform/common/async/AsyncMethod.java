package com.wellnr.platform.common.async;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncMethod {

    /**
     * If a method is set to `pure = true` it's expected that it doesn't change the state
     * behind the async boundary.
     */
    boolean pure();

}
