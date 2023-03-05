package com.wellnr.platform.common.tuples;

public final class Tuple {

    private Tuple() {

    }

    public static <T1, T2> Tuple2<T1, T2> apply(T1 _1, T2 _2) {
        return Tuple2.apply(_1, _2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> apply(T1 _1, T2 _2, T3 _3) {
        return Tuple3.apply(_1, _2, _3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> apply(T1 _1, T2 _2, T3 _3, T4 _4) {
        return Tuple4.apply(_1, _2, _3, _4);
    }

}
