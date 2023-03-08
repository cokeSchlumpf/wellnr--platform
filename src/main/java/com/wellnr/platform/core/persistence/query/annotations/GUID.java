package com.wellnr.platform.core.persistence.query.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to specify the path to the GUID field within an object.
 * This might be required if an object does not implement {@link com.wellnr.platform.common.guid.HasGUID}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface GUID {

    Path[] value();

}
