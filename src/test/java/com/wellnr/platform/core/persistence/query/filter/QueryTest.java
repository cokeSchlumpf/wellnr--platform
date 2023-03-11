package com.wellnr.platform.core.persistence.query.filter;

import com.wellnr.platform.core.persistence.inmemory.InMemoryQueryEngine;
import com.wellnr.platform.core.persistence.query.QueryEngineWithoutCustomQueries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import samples.data.car.Car;
import samples.data.car.Driver;
import samples.data.car.Engine;

import java.util.List;

import static com.wellnr.platform.core.persistence.query.Queries.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryTest {

    private QueryEngineWithoutCustomQueries<Car> engine;

    @BeforeEach
    public void setup() {
        this.engine = InMemoryQueryEngine.apply(Car.class, this.getSamples());
    }

    @Test
    public void testFindNestedFieldMatch() {
        var query = match(uppercase($("engine.type")), eq(uppercase(v("electric"))));
        var result = engine.findAll(query);

        assertEquals(1, result.size());
        assertEquals("Tesla", result.get(0).getBrand());
    }

    @Test
    public void testFindWithOr() {
        var query = or(
            match(uppercase($("brand")), eq(uppercase(v("audi")))),
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

    @Test
    public void testMatchWithComplexType() {
        var parameter = Car.apply("BMW", "marble", Engine.apply(210, "electric"), List.of());

        var query = match(
            $("brand"),
            eq($("brand", p(0)))
        );

        var result = engine.findAll(query, List.of(parameter));

        assertEquals(1, result.size());
        assertEquals("BMW", result.get(0).getBrand());
    }

    private List<Car> getSamples() {
        return List.of(
            Car.apply("audi", "red", Engine.apply(330, "gasoline"), List.of()),
            Car.apply("BMW", "yellow", Engine.apply(180, "petrol"), List.of(Driver.apply("michael", 29))),
            Car.apply("Tesla", "black", Engine.apply(180, "electric"), List.of()),
            Car.apply("Subaru", "silver", Engine.apply(180, "petrol"), List.of())
        );
    }

}