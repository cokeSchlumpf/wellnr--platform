package com.wellnr.platform.core.persistence.mongo;

import com.wellnr.platform.common.guid.HasGUID;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.core.config.MongoDatabaseConfiguration;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.query.QueryEngineRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class MongoRepository {


    @SuppressWarnings("unchecked")
    public static <R> R create(
        PlatformContext ctx,
        Class<R> repositoryType,
        MongoDatabaseConfiguration configuration,
        List<Tuple2<Class<? extends HasGUID>, MongoQueryEngine.MongoCollectionProperties>> entityTypes
    ) {
        return QueryEngineRepository.create(
            ctx,
            repositoryType,
            e -> entityTypes
                .stream()
                .filter(t -> t._1.equals(e))
                .map(t -> MongoQueryEngine.apply((Class<HasGUID>) t._1, configuration, t._2))
                .findFirst()
                .orElseThrow(),
            entityTypes.stream().map(t -> (Class<HasGUID>) t._1).toList()
        );
    }

    @SuppressWarnings("unchecked")
    public static <R> R create(
        PlatformContext ctx,
        Class<R> repositoryType,
        MongoDatabaseConfiguration configuration,
        Tuple2<Class<? extends HasGUID>, MongoQueryEngine.MongoCollectionProperties>... entityTypes
    ) {
        var entityTypesList = Arrays.stream(entityTypes).toList();
        return create(ctx, repositoryType, configuration, entityTypesList);
    }

    @SuppressWarnings("unchecked")
    public static <R> R create(
        PlatformContext ctx,
        Class<R> repositoryType,
        MongoDatabaseConfiguration configuration,
        Class<? extends HasGUID> entityType
    ) {
        return create(
            ctx,
            repositoryType,
            configuration,
            Tuple2.apply(entityType, MongoQueryEngine.MongoCollectionProperties.apply())
        );
    }

}
