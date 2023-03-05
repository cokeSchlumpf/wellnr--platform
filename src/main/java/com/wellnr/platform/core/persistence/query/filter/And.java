package com.wellnr.platform.core.persistence.query.filter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;

@lombok.Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class And implements Filter {

    List<Query> filters;

    public static And apply(List<Query> filters) {
        return new And(List.copyOf(filters));
    }

}
