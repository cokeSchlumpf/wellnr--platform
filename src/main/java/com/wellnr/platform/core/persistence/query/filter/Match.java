package com.wellnr.platform.core.persistence.query.filter;

import com.wellnr.platform.core.persistence.query.values.Value;
import lombok.AllArgsConstructor;

@lombok.Value
@AllArgsConstructor(staticName = "apply")
public class Match implements Filter {

    Value selector;

    Filter query;

}
