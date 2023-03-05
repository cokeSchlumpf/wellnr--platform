package com.wellnr.platform.core.persistence.query.values;

import lombok.AllArgsConstructor;

/**
 *
 */
@lombok.Value
@AllArgsConstructor(staticName = "apply")
public class StaticValue<T> implements Value {

    T value;

}
