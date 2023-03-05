package com.wellnr.platform.common.tuples;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class Tuple4<T1, T2, T3, T4> {

    public T1 _1;

    public T2 _2;

    public T3 _3;

    public T4 _4;

}
