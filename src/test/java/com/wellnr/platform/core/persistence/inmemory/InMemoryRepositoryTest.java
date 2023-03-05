package com.wellnr.platform.core.persistence.inmemory;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryRepositoryTest {

    @Test
    public void test() {
        var repo = InMemoryRepository.create(
            null, CarsRepo.class, Car.class
        );

        repo.insertOrUpdateCar(Car.apply(GUID.apply("cars", "bmw"), "BMW", "red", Engine.apply(10, "gas"), List.of()));
        repo.insertOrUpdateCar(Car.apply(GUID.apply("cars", "audi"), "Audi", "yellow", Engine.apply(10, "gas"), List.of()));

        var result = repo.findAllCarsByBrand("BMW");

        assertEquals(1, result.size());
        assertEquals("BMW", result.get(0).getBrand());

        repo.removeCarByGUID(GUID.apply("cars", "bmw"));

        result = repo.findAllCarsByBrand("BMW");
        assertEquals(0, result.size());

        result = repo.findAllCarsByBrand("Audi");
        assertEquals(1, result.size());
    }

    @Test
    public void testAsync() throws ExecutionException, InterruptedException {
        var repo = InMemoryRepository.create(
            null, CarsRepoAsync.class, Car.class
        );

        repo.insertOrUpdateCar(Car.apply(GUID.apply("cars", "bmw"), "BMW", "red", Engine.apply(10, "gas"), List.of())).toCompletableFuture().get();
        repo.insertOrUpdateCar(Car.apply(GUID.apply("cars", "audi"), "Audi", "yellow", Engine.apply(10, "gas"), List.of())).toCompletableFuture().get();

        var result = repo.findAllCarsByBrand("BMW").toCompletableFuture().get();

        assertEquals(1, result.size());
        assertEquals("BMW", result.get(0).getBrand());

        repo.removeCarByGUID(GUID.apply("cars", "bmw")).toCompletableFuture().get();

        result = repo.findAllCarsByBrand("BMW").toCompletableFuture().get();
        assertEquals(0, result.size());

        result = repo.findAllCarsByBrand("Audi").toCompletableFuture().get();
        assertEquals(1, result.size());
    }

    public interface CarsRepo {

        List<Car> findAllCarsByBrand(String brand);

        void insertOrUpdateCar(Car car);

        void removeCarByGUID(GUID guid);

    }

    public interface CarsRepoAsync {

        CompletionStage<List<Car>> findAllCarsByBrand(String brand);

        CompletionStage<Done> insertOrUpdateCar(Car car);

        CompletionStage<Done> removeCarByGUID(GUID guid);

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static class Car {

        GUID guid;

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