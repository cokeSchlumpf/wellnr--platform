package com.wellnr.platform.core.persistence.query;

import com.wellnr.platform.core.persistence.query.filter.Query;

import java.util.List;
import java.util.Optional;

public interface QueryEngine<T, C> {

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
     * @param query The query to find items to be deleted.
     */
    default void remove(Query query) {
        remove(query, List.of());
    }

    /**
     * Insert or update an entity in the database. The query will be extracted
     * based on custom annotations of the method.
     * <p>
     * This function is used by {@link QueryEngineRepository} when a method is detected to have a custom
     * query annotation.
     *
     * @param customQuery A custom query.
     */
    void insertOrUpdate(T item, C customQuery);

    /**
     * Find all matches within the database. The query will be extracted
     * based on custom annotations of the method.
     * <p>
     * This function is used by {@link QueryEngineRepository} when a method is detected to have a custom
     * query annotation.
     *
     * @param customQuery A custom query.
     * @return The result of the query.
     */
    List<T> findAll(C customQuery);

    /**
     * Find a single match within the database. The query will be extracted
     * based on custom annotations of the method.
     * <p>
     * This function is used by {@link QueryEngineRepository} when a method is detected to have a custom
     * query annotation.
     *
     * @param customQuery A custom query.
     * @return The result of the query.
     */
    Optional<T> findOne(C customQuery);

    /**
     * Remove an entity from the database. The query will be extracted
     * based on custom annotations of the method.
     * <p>
     * This function is used by {@link QueryEngineRepository} when a method is detected to have a custom
     * query annotation.
     *
     * @param customQuery A custom query.
     */
    void remove(C customQuery);
}
