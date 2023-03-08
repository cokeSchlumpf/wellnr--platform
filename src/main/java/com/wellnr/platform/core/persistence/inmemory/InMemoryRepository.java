package com.wellnr.platform.core.persistence.inmemory;

import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.query.QueryEngineRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class InMemoryRepository {

    @SuppressWarnings("unchecked")
    public static <R> R create(
        PlatformContext ctx, Class<R> repositoryType, Class<?>... entityTypes
    ) {
        return QueryEngineRepository.create(
            ctx,
            repositoryType,
            InMemoryQueryEngine::apply,
            Arrays.stream(entityTypes).map(t -> (Class<Object>) t).toList()
        );
    }

}
