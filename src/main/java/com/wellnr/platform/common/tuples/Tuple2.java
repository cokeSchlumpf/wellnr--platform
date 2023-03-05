package com.wellnr.platform.common.tuples;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class Tuple2<T1, T2> {

    public T1 _1;

    public T2 _2;

}
