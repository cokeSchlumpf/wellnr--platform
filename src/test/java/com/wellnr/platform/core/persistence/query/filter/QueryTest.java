package com.wellnr.platform.core.persistence.query.filter;

import com.wellnr.platform.core.persistence.inmemory.QueryEngine;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.wellnr.platform.core.persistence.query.Queries.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryTest {

    @Test
    public void testFindNestedFieldMatch() {
        var query = match(uppercase($("engine.type")), eq(uppercase(v("electric"))));
        var engine = QueryEngine.apply(Car.class, getSamples(), query, List.of());
        var result = engine.findAll();

        assertEquals(1, result.size());
        assertEquals("Tesla", result.get(0).getBrand());
    }

    @Test
    public void testFindWithOr() {
        var query = or(
            match(uppercase($("brand")), eq(uppercase(v("audi")))),
            match($("color"), eq(v("black")))
        );
        var engine = QueryEngine.apply(Car.class, getSamples(), query, List.of());
        var result = engine.findAll();

        assertEquals(2, result.size());
    }

    @Test
    public void testFindMatchesInChildCollections() {
        var query = match(
            $("drivers"),
            match($("name"), eq(v("michael"))));


        var engine = QueryEngine.apply(Car.class, getSamples(), query, List.of());
        var result = engine.findAll();

        assertEquals(1, result.size());
        assertEquals("BMW", result.get(0).getBrand());
    }

    @Test
    public void testFindMatchesWithInt() {
        var query = match(
            $("drivers"),
            match($("age"), eq(v(29))));


        var engine = QueryEngine.apply(Car.class, getSamples(), query, List.of());
        var result = engine.findAll();

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

        var engine = QueryEngine.apply(Car.class, getSamples(), query, List.of(parameter));
        var result = engine.findAll();

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

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static class Car {

        String brand;

        String color;

        Engine engine;

        List<Driver> drivers;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static class Engine {

        int power;

        String type;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static class Driver {

        String name;

        int age;

    }

}