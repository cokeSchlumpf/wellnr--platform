package com.wellnr.platform.core.persistence.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.wellnr.platform.common.databind.DefaultObjectMapperFactory;
import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.guid.HasGUID;
import com.wellnr.platform.common.tuples.Either;
import com.wellnr.platform.common.tuples.Nothing;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.common.tuples.Tuple2;
import com.wellnr.platform.core.config.MongoDatabaseConfiguration;
import com.wellnr.platform.core.persistence.query.QueryEngine;
import com.wellnr.platform.core.persistence.query.filter.*;
import com.wellnr.platform.core.persistence.query.values.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.mongojack.JacksonMongoCollection;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.*;

/**
 * {@link MongoQueryEngine} translates {@link Query} expressions into Bson queries and executes them
 * towards a Mongo database.
 * <p>
 * Not all allowed expressions from {@link Query} can be mapped into Bson queries. In these cases the
 * class will throw exceptions.
 * <p>
 * When using {@link MongoQueryEngine}, make sure that type <code>T</code> has annotated its
 * {@link com.wellnr.platform.common.guid.GUID} field, and the GUID parameter
 * {@link com.fasterxml.jackson.annotation.JsonCreator} if present, with {@link org.mongojack.ObjectId}.
 *
 * @param <T> The type of the entity class to store in Mongo.
 */
@AllArgsConstructor(staticName = "apply")
public final class MongoQueryEngine<T extends HasGUID> implements QueryEngine<T> {

    /**
     * The type of the entity class to store in Mongo.
     */
    // Class<T> type;

    /**
     * The mongo database client.
     */
    JacksonMongoCollection<T> collection;

    /**
     * Creates a new instance.
     *
     * @param type                 The type of the collection.
     * @param database             The Mongo database to be used.
     * @param collectionProperties Additional configurations for the collection.
     * @param <T>                  The type of the collection.
     * @return A new instance.
     */
    public static <T extends HasGUID> MongoQueryEngine<T> apply(
        Class<T> type,
        MongoDatabase database,
        MongoCollectionProperties collectionProperties
    ) {
        var collection = JacksonMongoCollection
            .builder()
            .withObjectMapper(collectionProperties.getObjectMapper().orElseGet(() ->
                DefaultObjectMapperFactory.apply().createJsonMapper(true)
            ))
            .build(
                database,
                collectionProperties.getCollectionName().orElseGet(() -> type.getSimpleName().toLowerCase()),
                type,
                UuidRepresentation.STANDARD);

        return apply(collection);
    }

    /**
     * Creates a new instance.
     *
     * @param type     The type of the collection.
     * @param database The Mongo database to be used.
     * @param <T>      The type of the collection.
     * @return A new instance.
     */
    public static <T extends HasGUID> MongoQueryEngine<T> apply(
        Class<T> type,
        MongoDatabase database
    ) {
        return apply(type, database, MongoCollectionProperties.apply());
    }

    /**
     * Creates a new instance.
     *
     * @param type                 The type of the collection.
     * @param configuration        Connection configuration for the Mongo database.
     * @param collectionProperties Additional configurations for the collection.
     * @param <T>                  The type of the collection.
     * @return A new instance.
     */
    public static <T extends HasGUID> MongoQueryEngine<T> apply(
        Class<T> type,
        MongoDatabaseConfiguration configuration,
        MongoCollectionProperties collectionProperties) {

        return apply(type, configuration.getClient(), collectionProperties);
    }

    /**
     * Creates a new instance.
     *
     * @param type          The type of the collection.
     * @param configuration Connection configuration for the Mongo database.
     * @param <T>           The type of the collection.
     * @return A new instance.
     */
    public static <T extends HasGUID> MongoQueryEngine<T> apply(
        Class<T> type,
        MongoDatabaseConfiguration configuration) {

        return apply(type, configuration.getClient());
    }

    @Override
    public void insertOrUpdate(
        T item,
        Query match,
        List<Object> parameters
    ) {
        var options = new ReplaceOptions().upsert(true);
        this.collection.replaceOne(mapToCondition(match, parameters), item, options);
    }

    @Override
    public void insertOrUpdate(
        T item,
        Query match
    ) {
        insertOrUpdate(item, match, List.of());
    }

    @Override
    public List<T> findAll(Query query, List<Object> parameters) {
        var bson = mapToCondition(query, parameters);

        System.out.println(bson.toBsonDocument());

        return StreamSupport
            .stream(
                this.collection.find(bson).spliterator(), false
            )
            .toList();
    }

    @Override
    public List<T> findAll(Query query) {
        return findAll(query, List.of());
    }

    @Override
    public Optional<T> findOne(Query query, List<Object> parameters) {
        return Optional.ofNullable(
            this.collection.findOne(mapToCondition(query, parameters))
        );
    }

    @Override
    public Optional<T> findOne(Query query) {
        return findOne(query, List.of());
    }

    @Override
    public void remove(Query query, List<Object> parameters) {
        // Handle common case ...
        if ((query instanceof Match match) && match.getQuery() instanceof Equals eq){
            var value = this.resolveValue(eq.getValue(), parameters);

            if (value.isLeft()) {
                collection.removeById(value.getLeft());
            } else {
                removeFromQuery(query, parameters);
            }
        } else {
            removeFromQuery(query, parameters);
        }
    }

    private void removeFromQuery(Query query, List<Object> parameters) {
        this
            .findAll(query, parameters)
            .forEach(entity -> {
                collection.removeById(entity.getGUID().toString());
            });
    }

    private Bson mapToCondition(Query query, List<Object> parameters) {
        if (query instanceof Match match) {
            return resolveMatch(match, parameters);
        } else if (query instanceof ElemMatch elemMatch) {
            return resolveElemMatch(elemMatch, parameters);
        } else if (query instanceof And and) {
            return resolveAnd(and, parameters);
        } else if (query instanceof Or or) {
            return resolveOr(or, parameters);
        }

        throw new IllegalArgumentException(MessageFormat.format(
            "Can''t handle query `{0}` at this level.",
            query
        ));
    }

    private Bson resolveOr(Or orQuery, List<Object> parameters) {
        return or(
            orQuery
                .getFilters()
                .stream()
                .map(q -> this.mapToCondition(q, parameters))
                .toList()
        );
    }

    private Bson resolveAnd(And andQuery, List<Object> parameters) {
        return and(
            andQuery
                .getFilters()
                .stream()
                .map(q -> this.mapToCondition(q, parameters))
                .toList()
        );
    }

    private Bson resolveElemMatch(ElemMatch elemMatch, List<Object> parameters) {
        if (elemMatch.getSelector() instanceof Field field) {
            return elemMatch(field.getFQN(), mapToCondition(elemMatch.getQuery(), parameters));
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                "MongoQueryEngine does not support different selector's than field for `elemMatch` operator." +
                    " The following query cannot be transformed to Mongo query: `{0}`",
                elemMatch
            ));
        }
    }

    private Bson resolveMatch(Match match, List<Object> parameters) {
        if (match.getSelector() instanceof Field field) {
            if (match.getQuery() instanceof Equals equals) {
                var equalsValue = this.resolveValue(equals.getValue(), parameters);

                if (equalsValue.isLeft()) {
                    return eq(field.getFQN(), equalsValue.getLeftForce());
                }
            }
        }

        throw new IllegalArgumentException(MessageFormat.format(
            "Query `{0}` is not supported within this engine.",
            match
        ));
    }

    private Either<Object, Document> resolveValue(Value value, List<Object> parameters) {
        if (value instanceof StaticValue staticValue) {
            return Either.fromLeft(staticValue.getValue());
        } else if (value instanceof ParameterReference ref) {
            return Either.fromLeft(parameters.get(ref.getIndex()));
        } else if (value instanceof Select select) {
            var selectFromValue = resolveValue(select.getValue(), parameters);

            if (selectFromValue.isLeft()) {
                var object = selectFromValue.getLeftForce();
                var field = select.getSelect();
                return Either.fromLeft(resolveValueFromField(field, object));
            }
        } else if (value instanceof Uppercase uppercase) {
            var upperValue = resolveValue(uppercase.getValue(), parameters);
            return Either.fromLeft(upperValue.toString().toUpperCase());
        }

        throw new IllegalArgumentException(MessageFormat.format(
            "Value `{0}` cannot be resolved within this engine",
            value
        ));
    }

    @SuppressWarnings("unchecked")
    private <U> Object resolveValueFromField(Field field, U obj) {
        var objFieldTuple = getValueFromObject(field.getName(), (Class<U>) obj.getClass());
        var objFieldGetter = objFieldTuple._1;

        var childField = field.getChildField();
        var nextObj = objFieldGetter.get(obj);

        if (childField.isPresent()) {
            return resolveValueFromField(childField.get(), nextObj);
        } else {
            if (Objects.nonNull(nextObj)) {
                return nextObj;
            } else {
                return Nothing.getInstance();
            }
        }
    }

    private <U> Tuple2<Function1<U, Object>, Class<?>> getValueFromObject(String field, Class<U> type) {
        /*
         * Try to find getter.
         */
        var getter = Arrays
            .stream(type.getMethods())
            .filter(m -> m.getName().equalsIgnoreCase("get" + field))
            .filter(m -> m.getParameters().length == 0)
            .findFirst();

        var objField = Arrays
            .stream(type.getFields())
            .filter(m -> m.getName().equalsIgnoreCase(field))
            .findFirst();

        if (getter.isPresent()) {
            return Tuple.apply(
                obj -> getter.get().invoke(obj),
                getter.get().getReturnType()
            );
        } else if (objField.isPresent()) {
            objField.get().setAccessible(true);

            return Tuple.apply(
                obj -> objField.get().get(obj),
                objField.get().getType()
            );
        } else {
            throw new RuntimeException(MessageFormat.format(
                "Can''t find field `{0}` within type `{1}`",
                field, type.getName()
            ));
        }
    }

    /**
     * Optional configurations to instantiate {@link MongoQueryEngine}.
     */
    @With
    @lombok.Value
    @NoArgsConstructor(force = true, staticName = "apply")
    @AllArgsConstructor(staticName = "apply")
    public static class MongoCollectionProperties {

        /**
         * The name of the Mongo collection.
         */
        String collectionName;

        /**
         * The object mapper used to serialize and deserialize entity instances.
         */
        ObjectMapper objectMapper;

        public Optional<String> getCollectionName() {
            return Optional.ofNullable(collectionName);
        }

        public Optional<ObjectMapper> getObjectMapper() {
            return Optional.ofNullable(objectMapper);
        }

    }

}
