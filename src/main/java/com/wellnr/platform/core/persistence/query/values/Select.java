package com.wellnr.platform.core.persistence.query.values;

import lombok.AllArgsConstructor;

/**
 *
 */
@lombok.Value
@AllArgsConstructor(staticName = "apply")
public class Select implements Value {

    Value value;

    Field select;

}
