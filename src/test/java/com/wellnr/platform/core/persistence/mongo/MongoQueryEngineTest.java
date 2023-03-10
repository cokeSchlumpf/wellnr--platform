package com.wellnr.platform.core.persistence.mongo;

import com.wellnr.platform.common.databind.DefaultObjectMapperFactory;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.config.MongoDatabaseConfiguration;
import org.bson.UuidRepresentation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mongojack.JacksonMongoCollection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import samples.data.car.Car;
import samples.data.car.Driver;
import samples.data.car.Engine;

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
    public void testGUID() {
        var result = engine.findAll(match($("guid"), eq(v("/abc"))));
        assertEquals(1, result.size());

        result = engine.findAll(match($("guid"), eq(v(GUID.apply("def")))));
        assertEquals(1, result.size());
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

}
