package com.wellnr.platform.core.persistence.query.filter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;

@lombok.Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Or implements Filter {

    List<Query> filters;

    public static Or apply(List<Query> filters) {
        return new Or(List.copyOf(filters));
    }

}
