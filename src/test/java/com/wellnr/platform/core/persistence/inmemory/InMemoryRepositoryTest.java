package com.wellnr.platform.core.persistence.inmemory;

import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.query.AbstractQueryEngineRepositoryTest;
import samples.data.car.Car;
import samples.data.car.CarsRepository;

class InMemoryRepositoryTest extends AbstractQueryEngineRepositoryTest {


    @Override
    public CarsRepository getRepository(PlatformContext context) {
        return InMemoryRepository.create(context, CarsRepository.class, Car.class);
    }

}