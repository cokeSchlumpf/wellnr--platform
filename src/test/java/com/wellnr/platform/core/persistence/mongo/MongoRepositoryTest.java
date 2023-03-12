package com.wellnr.platform.core.persistence.mongo;

import com.wellnr.platform.core.config.MongoDatabaseConfiguration;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.persistence.query.AbstractQueryEngineRepositoryTest;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import samples.data.car.Car;
import samples.data.car.CarsRepository;
import samples.data.car.LogbookEntry;
import samples.data.car.LogbookEntryRepository;

public class MongoRepositoryTest extends AbstractQueryEngineRepositoryTest {

    private static final MongoDBContainer mongoDBContainer =
        new MongoDBContainer(DockerImageName.parse("mongo:6.0.4")).withReuse(true);

    @BeforeAll
    static void beforeAll() {
        mongoDBContainer.start();
    }

    @Override
    public CarsRepository getCarsRepository(PlatformContext context) {
        var config = MongoDatabaseConfiguration.apply(
            "test_database",
            mongoDBContainer.getConnectionString()
        );

        return MongoRepository.create(context, CarsRepository.class, config, Car.class);
    }

    @Override
    public LogbookEntryRepository getLogbookEntriesRepository(PlatformContext context) {
        var config = MongoDatabaseConfiguration.apply(
            "test_database",
            mongoDBContainer.getConnectionString()
        );

        return MongoRepository.create(
            context,
            LogbookEntryRepository.class,
            config,
            LogbookEntry.class);
    }

}
