package com.wellnr.platform.core.persistence.query;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.context.PlatformContext;
import org.junit.jupiter.api.Test;
import samples.data.car.Car;
import samples.data.car.CarsRepository;
import samples.data.car.Engine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractQueryEngineRepositoryTest {

    public abstract CarsRepository getRepository(PlatformContext context);

    @Test
    public void test() {
        var repo = getRepository(null);
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

        var singleResult = repo.findOneCarByGUID(GUID.apply("cars", "audi"));
        assertTrue(singleResult.isPresent());

        repo.insertOrUpdateCar(Car.apply(GUID.apply("cars", "tesla"), "Tesla", "silver", Engine.apply(110, "electric"), List.of()));
        result = repo.findAllCars();
        assertEquals(2, result.size());
    }

}
