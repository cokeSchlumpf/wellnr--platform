package samples.services;

import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.modules.users.values.users.User;
import com.wellnr.platform.core.services.GeneratedImpl;

import java.util.concurrent.CompletionStage;

import static com.wellnr.platform.common.Operators.pass;

public interface CarServices {

    @GeneratedImpl(delegate = CarsRootEntity.class)
    default CompletionStage<Done> createCar(User user, String brand, String name) {
        return pass();
    }

    @GeneratedImpl(
        delegate = CarRootEntity.class,
        lookup = CarsRootEntity.class
    )
    default CompletionStage<Done> inspect(User user, String name, String comment) {
        return pass();
    }

    @GeneratedImpl(
        delegate = CarRootEntity.class,
        lookup = CarsRootEntity.class,
        permissions = {
            CarPermissions.MANAGE_CARS,
            CarPermissions.MANAGE_CAR
        }
    )
    default CompletionStage<String> getCurrentLocation(User user, String name) {
        return pass();
    }

    CompletionStage<String> someComplexMethod(User user, int value);

}
