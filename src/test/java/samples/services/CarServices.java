package samples.services;

import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.modules.users.values.users.User;
import com.wellnr.platform.core.services.GeneratedImpl;

import java.util.concurrent.CompletionStage;

public interface CarServices {

    @GeneratedImpl(delegate = CarsRootEntity.class)
    CompletionStage<Done> createCar(User user, String brand, String name);

    @GeneratedImpl(
        delegate = CarRootEntity.class,
        lookup = CarsRootEntity.class
    )
    CompletionStage<Done> inspect(User user, String name, String comment);

    @GeneratedImpl(
        delegate = CarRootEntity.class,
        lookup = CarsRootEntity.class
    )
    CompletionStage<String> getCurrentLocation(User user, String name);

}
