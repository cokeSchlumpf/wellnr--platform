package com.wellnr.platform.core.persistence;

import java.util.List;

public final class Operations {

    public static final String FIND_ALL = "findAll";
    public static final String FIND_ONE = "findOne";
    public static final String UPSERT = "insertOrUpdate";
    public static final String REMOVE = "remove";

    public static final List<String> ALL_OPERATIONS = List.of(FIND_ALL, FIND_ONE, UPSERT, REMOVE);

}
