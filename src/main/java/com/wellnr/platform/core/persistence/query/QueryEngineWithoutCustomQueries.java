package com.wellnr.platform.core.persistence.query;

import com.wellnr.platform.common.tuples.Nothing;

import java.util.List;
import java.util.Optional;

public interface QueryEngineWithoutCustomQueries<T> extends QueryEngine<T, Nothing> {

    /**
     * Insert or update an entity in the database. The query will be extracted
     * based on custom annotations of the method.
     * <p>
     * This function is used by {@link AbstractQueryEngineRepositoryFactory} when a method is detected to have a custom
     * query annotation.
     *
     * @param customQuery A custom query.
     */
    default void insertOrUpdate(T item, Nothing customQuery) {
        throw new IllegalStateException(
            "This QueryEngine does not support queries based on custom annotations."
        );
    }

    /**
     * Find all matches within the database. The query will be extracted
     * based on custom annotations of the method.
     * <p>
     * This function is used by {@link AbstractQueryEngineRepositoryFactory} when a method is detected to have a custom
     * query annotation.
     *
     * @param customQuery A custom query.
     * @return The result of the query.
     */
    default List<T> findAll(Nothing customQuery) {
        throw new IllegalStateException(
            "This QueryEngine does not support queries based on custom annotations."
        );
    }

    /**
     * Find a single match within the database. The query will be extracted
     * based on custom annotations of the method.
     * <p>
     * This function is used by {@link AbstractQueryEngineRepositoryFactory} when a method is detected to have a custom
     * query annotation.
     *
     * @param customQuery A custom query.
     * @return The result of the query.
     */
    default Optional<T> findOne(Nothing customQuery) {
        throw new IllegalStateException(
            "This QueryEngine does not support queries based on custom annotations."
        );
    }

    /**
     * Remove an entity from the database. The query will be extracted
     * based on custom annotations of the method.
     * <p>
     * This function is used by {@link AbstractQueryEngineRepositoryFactory} when a method is detected to have a custom
     * query annotation.
     *
     * @param customQuery A custom query.
     */
    default void remove(Nothing customQuery) {
        throw new IllegalStateException(
            "This QueryEngine does not support queries based on custom annotations."
        );
    }

}
