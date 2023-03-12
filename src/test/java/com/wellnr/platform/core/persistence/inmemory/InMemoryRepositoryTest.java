package com.wellnr.platform.core.persistence.inmemory;

import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.query.AbstractQueryEngineRepositoryTest;
import samples.data.car.Car;
import samples.data.car.CarsRepository;
import samples.data.car.LogbookEntry;
import samples.data.car.LogbookEntryRepository;

class InMemoryRepositoryTest extends AbstractQueryEngineRepositoryTest {


    @Override
    public CarsRepository getCarsRepository(PlatformContext context) {
        return InMemoryRepository.create(context, CarsRepository.class, Car.class);
    }

    @Override
    public LogbookEntryRepository getLogbookEntriesRepository(PlatformContext context) {
        return InMemoryRepository.create(context, LogbookEntryRepository.class, LogbookEntry.class);
    }

}