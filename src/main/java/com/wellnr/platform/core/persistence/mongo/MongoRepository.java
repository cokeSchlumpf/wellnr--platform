package com.wellnr.platform.core.persistence.mongo;

import com.wellnr.platform.common.guid.HasGUID;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.core.config.MongoDatabaseConfiguration;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.query.AbstractQueryEngineRepositoryFactory;
import com.wellnr.platform.core.persistence.query.QueryEngine;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MongoRepository extends AbstractQueryEngineRepositoryFactory<HasGUID, Bson> {

    private final MongoDatabaseConfiguration configuration;

    private final Map<Class<HasGUID>, MongoQueryEngine.MongoCollectionProperties> entityTypes;

    @SuppressWarnings("unchecked")
    public static <R> R create(
        PlatformContext ctx,
        Class<R> repositoryType,
        MongoDatabaseConfiguration configuration,
        List<Tuple2<Class<? extends HasGUID>, MongoQueryEngine.MongoCollectionProperties>> entityTypes
    ) {
        var factory = new MongoRepository(
            configuration,
            entityTypes.stream().collect(Collectors.toMap(t -> (Class<HasGUID>) t._1, Tuple2::get_2))
        );

        return factory.create(
            ctx,
            repositoryType,
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

    @Override
    protected QueryEngine<HasGUID, Bson> createQueryEngine(Class<HasGUID> entityType) {
        return MongoQueryEngine.apply(
            entityType, configuration, this.entityTypes.get(entityType)
        );
    }

}
