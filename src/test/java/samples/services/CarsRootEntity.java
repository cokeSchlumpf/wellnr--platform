package samples.services;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.async.AsyncMethod;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.context.RootEntity;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public class CarsRootEntity implements RootEntity {

    @Override
    public GUID getGUID() {
        return GUID.fromString("/app/cars");
    }

    @AsyncMethod(pure = true)
    public CompletionStage<CarRootEntity> getCarRootEntityByName(String name) {
        return CompletableFuture.completedFuture(CarRootEntity.apply(GUID.apply("app", "cars", name)));
    }

    @AsyncMethod(pure = false)
    public CompletionStage<Done> createCar(String brand, String name) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Creating a car ...");
            Operators.suppressExceptions(() -> Thread.sleep(400));
            return Done.getInstance();
        });
    }

}
