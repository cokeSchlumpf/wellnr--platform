package com.wellnr.platform.core.persistence.query.filter;

import com.wellnr.platform.core.persistence.query.values.Value;
import lombok.AllArgsConstructor;

import java.util.List;

@lombok.Value
@AllArgsConstructor(staticName = "apply")
public class IsIn implements Filter {

    List<Value> values;

}
