package com.wellnr.platform.core.persistence.query;

import com.wellnr.platform.core.persistence.query.filter.Query;

import java.util.List;
import java.util.Optional;

public interface QueryEngine<T> {
    /**
     * Inserts or updates an instance.
     *
     * @param item       The item to be inserted/ updated.
     * @param match      The query to identify the matching item to be updated if present.
     * @param parameters The list of parameters which might be referenced in match.
     */
    void insertOrUpdate(
        T item,
        Query match,
        List<Object> parameters
    );

    /**
     * Inserts or updates an instance.
     *
     * @param item  The item to be inserted/ updated.
     * @param match The query to identify the matching item to be updated if present.
     */
    default void insertOrUpdate(
        T item,
        Query match
    ) {
        insertOrUpdate(item, match, List.of());
    }

    /**
     * Find all matches of the query within the database.
     *
     * @param query      The query to filter entities.
     * @param parameters Parameters which may be referenced in the query.
     * @return The list of results.
     */
    List<T> findAll(Query query, List<Object> parameters);

    /**
     * Find all matches of the query within the database.
     *
     * @param query The query to filter entities.
     * @return The list of results.
     */
    default List<T> findAll(Query query) {
        return findAll(query, List.of());
    }

    /**
     * Find at most one match of the query within the database.
     *
     * @param query      The query to filter entities.
     * @param parameters Parameters which may be referenced in the query.
     * @return The list of results.
     */
    Optional<T> findOne(Query query, List<Object> parameters);

    /**
     * Find at most one match of the query within the database.
     *
     * @param query The query to filter entities.
     * @return The list of results.
     */
    default Optional<T> findOne(Query query) {
        return findOne(query, List.of());
    }

    /**
     * Remove entities from the database matching a condition.
     *
     * @param query      The query to find items to be deleted.
     * @param parameters Parameters which may be referenced in the query.
     */
    void remove(Query query, List<Object> parameters);

    /**
     * Remove entities from the database matching a condition.
     *
     * @param query      The query to find items to be deleted.
     */
    default void remove(Query query) {
        remove(query, List.of());
    }
}
