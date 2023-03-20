package com.wellnr.platform.core.services;

import com.wellnr.platform.core.context.RootEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedImpl {

    /**
     * The root entity to which the call should be delegated.
     *
     * @return The delegation entity.
     */
    Class<? extends RootEntity> delegate();

    /**
     * Entity which should be used to lookup actual delegation entity. There must be either 0 or 1 lookup entities.
     * Never multiple.
     *
     * @return Lookup entities.
     */
    Class<? extends RootEntity>[] lookup() default {};

    String[] permissions() default {};

}
