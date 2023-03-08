package com.wellnr.platform.core.persistence.mongo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wellnr.platform.common.databind.DefaultObjectMapperFactory;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.guid.HasGUID;
import com.wellnr.platform.core.config.MongoDatabaseConfiguration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.bson.UuidRepresentation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.ObjectId;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static com.wellnr.platform.core.persistence.query.Queries.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MongoQueryEngineTest {

    private static final MongoDBContainer mongoDBContainer =
        new MongoDBContainer(DockerImageName.parse("mongo:6.0.4")).withReuse(true);

    private static MongoQueryEngine<Car> engine;

    @BeforeAll
    static void beforeAll() {
        mongoDBContainer.start();

        var config = MongoDatabaseConfiguration.apply(
            "test",
            mongoDBContainer.getConnectionString()
        );

        var database = config.getClient();
        var cars = JacksonMongoCollection
            .builder()
            .withObjectMapper(DefaultObjectMapperFactory.apply().createJsonMapper(true))
            .build(database, "applications", Car.class, UuidRepresentation.STANDARD);

        cars.insert(List.of(
            Car.apply(GUID.apply("abc"), "audi", "red", Engine.apply(330, "gasoline"), List.of()),
            Car.apply(GUID.apply("def"), "BMW", "yellow", Engine.apply(180, "petrol"), List.of(Driver.apply("michael"
                , 29))),
            Car.apply(GUID.apply("ghi"), "Tesla", "black", Engine.apply(180, "electric"), List.of()),
            Car.apply(GUID.apply("jkl"), "Subaru", "silver", Engine.apply(180, "petrol"), List.of())
        ));

        engine = MongoQueryEngine.apply(cars);
    }

    @Test
    public void test() {
        var result = engine.findAll(match($("brand"), eq(v("BMW"))));
        System.out.println(result);
    }

    @Test
    public void testFindNestedFieldMatch() {
        var query = match($("engine.type"), eq(v("electric")));
        var result = engine.findAll(query);

        assertEquals(1, result.size());
        assertEquals("Tesla", result.get(0).getBrand());
    }

    @Test
    public void testFindWithOr() {
        var query = or(
            match($("brand"), eq(v("audi"))),
            match($("color"), eq(v("black")))
        );
        var result = engine.findAll(query);

        assertEquals(2, result.size());
    }

    @Test
    public void testFindMatchesInChildCollections() {
        var query = elemMatch(
            $("drivers"),
            match($("name"), eq(v("michael"))));

        var result = engine.findAll(query);

        assertEquals(1, result.size());
        assertEquals("BMW", result.get(0).getBrand());
    }

    @Test
    public void testFindMatchesWithInt() {
        var query = elemMatch(
            $("drivers"),
            match($("age"), eq(v(29))));

        var result = engine.findAll(query);

        assertEquals(1, result.size());
        assertEquals("BMW", result.get(0).getBrand());
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Car implements HasGUID {

        private static final String GUID = "_id";
        private static final String BRAND = "brand";
        private static final String COLOR = "color";
        private static final String ENGINE = "engine";
        private static final String DRIVERS = "drivers";

        @ObjectId
        @JsonProperty(GUID)
        GUID guid;

        @JsonProperty(BRAND)
        String brand;

        @JsonProperty(COLOR)
        String color;

        @JsonProperty(ENGINE)
        Engine engine;

        @JsonProperty(DRIVERS)
        List<Driver> drivers;

        @JsonCreator
        public static Car apply(
            @ObjectId @JsonProperty(GUID) GUID guid,
            @JsonProperty(BRAND) String brand,
            @JsonProperty(COLOR) String color,
            @JsonProperty(ENGINE) Engine engine,
            @JsonProperty(DRIVERS) List<Driver> drivers
        ) {
            return new Car(guid, brand, color, engine, drivers);
        }

        @Override
        public GUID getGUID() {
            return this.guid;
        }
    }

    @Value
    @NoArgsConstructor(force = true)
    @AllArgsConstructor(staticName = "apply")
    public static class Engine {

        int power;

        String type;

    }

    @Value
    @NoArgsConstructor(force = true)
    @AllArgsConstructor(staticName = "apply")
    public static class Driver {

        String name;

        int age;

    }

}
