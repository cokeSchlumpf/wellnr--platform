package com.wellnr.platform.core.persistence.inmemory;

import com.wellnr.platform.common.tuples.Nothing;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.query.AbstractQueryEngineRepositoryFactory;
import com.wellnr.platform.core.persistence.query.QueryEngine;

import java.util.Arrays;

public final class InMemoryRepository extends AbstractQueryEngineRepositoryFactory<Object, Nothing> {

    public InMemoryRepository(PlatformContext ctx) {
        super(ctx);
    }

    @SuppressWarnings("unchecked")
    public static <R> R create(
        PlatformContext ctx, Class<R> repositoryType, Class<?>... entityTypes
    ) {
        var factory = new InMemoryRepository(ctx);

        return factory.create(
            ctx,
            repositoryType,
            Arrays.stream(entityTypes).map(t -> (Class<Object>) t).toList()
        );
    }

    @Override
    protected QueryEngine<Object, Nothing> createQueryEngine(Class<Object> entityType, Class<Object> mementoType) {
        return InMemoryQueryEngine.apply(mementoType);
    }
}
