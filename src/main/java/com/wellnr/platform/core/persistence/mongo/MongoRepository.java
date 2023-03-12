package com.wellnr.platform.core.persistence.mongo;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.guid.HasGUID;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.core.config.MongoDatabaseConfiguration;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.query.AbstractQueryEngineRepositoryFactory;
import com.wellnr.platform.core.persistence.query.QueryEngine;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MongoRepository extends AbstractQueryEngineRepositoryFactory<HasGUID, Bson> {

    private final MongoDatabaseConfiguration configuration;

    private final Map<Class<HasGUID>, MongoQueryEngine.MongoCollectionProperties> entityTypes;

    private MongoRepository(
        PlatformContext ctx,
        MongoDatabaseConfiguration configuration,
        Map<Class<HasGUID>, MongoQueryEngine.MongoCollectionProperties> entityTypes) {

        super(ctx);
        this.configuration = configuration;
        this.entityTypes = entityTypes;
    }

    @SuppressWarnings("unchecked")
    public static <R> R create(
        PlatformContext ctx,
        Class<R> repositoryType,
        MongoDatabaseConfiguration configuration,
        List<Tuple2<Class<? extends HasGUID>, MongoQueryEngine.MongoCollectionProperties>> entityTypes
    ) {
        var factory = new MongoRepository(
            ctx,
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
    protected QueryEngine<HasGUID, Bson> createQueryEngine(Class<HasGUID> entityType, Class<HasGUID> mementoType) {
        return MongoQueryEngine.apply(
            mementoType, configuration, this.entityTypes.get(entityType)
        );
    }

    @Override
    protected Optional<Function1<List<Object>, Bson>> getCustomQueryFromMethod(Method m) {
        var maybeAnnotation = Optional.ofNullable(m.getAnnotation(CustomMongoQuery.class));

        if (maybeAnnotation.isPresent()) {
            var queryDefinitionClass = maybeAnnotation.get().value();
            var methodName = maybeAnnotation.get().methodName().length() > 0 ? maybeAnnotation.get()
                .methodName() : m.getName();
            var queryFactory = Operators.suppressExceptions(() -> queryDefinitionClass.getMethod(methodName));
            var resultType = queryFactory.getReturnType();

            if (!resultType.equals(Bson.class)) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Method `{0}#{1}` must have return type Bson.",
                    queryDefinitionClass.getName(), queryFactory.getName()
                ));
            } else if (queryFactory.getParameters().length != 1 || !queryFactory.getParameters()[0].getType().equals(List.class)) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Method `{0}#{1}` must accept one parameter of type List<Object> for accepting arguments",
                    queryDefinitionClass.getName(), queryFactory.getName()
                ));
            }

            return Optional.of(args -> (Bson) queryFactory.invoke(args));
        } else {
            return super.getCustomQueryFromMethod(m);
        }
    }
}
