package com.wellnr.platform.core.persistence.query;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.context.PlatformContext;
import org.junit.jupiter.api.Test;
import samples.data.car.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractQueryEngineRepositoryTest {

    public abstract CarsRepository getCarsRepository(PlatformContext context);

    public abstract LogbookEntryRepository getLogbookEntriesRepository(PlatformContext context);

    @Test
    public void test() {
        var repo = getCarsRepository(null);
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

        var singleResultGet = repo.getCarByGUID(GUID.apply("cars", "tesla"));
        assertEquals(singleResultGet.getBrand(), "Tesla");
    }

    @Test
    public void testMementoRepo() {
        var car = Car.apply(
            GUID.apply("cars", "bmw"), "BMW", "red", Engine.apply(10, "gas"), List.of()
        );

        var entry = LogbookEntry.apply(
            GUID.apply("abc"), car, "Plauen", "Merzig"
        );

        var context = PlatformContext.apply();
        var cars = getCarsRepository(context);
        var entries = getLogbookEntriesRepository(context);

        context
            .withSingletonInstance(cars, CarsRepository.class)
            .withSingletonInstance(entries, LogbookEntryRepository.class)
            .initialize();

        cars.insertOrUpdateCar(car);
        entries.insertOrUpdateLogbookEntry(entry);

        var result = entries.findAllLogbookEntriesByFrom("Plauen");

        assertEquals(1, result.size());
        assertEquals(entry, result.get(0));
    }

}
