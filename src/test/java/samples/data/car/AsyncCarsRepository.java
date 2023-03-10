package samples.data.car;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface AsyncCarsRepository {

    CompletionStage<Optional<Car>> findOneCarByGUID(GUID guid);

    CompletionStage<List<Car>> findAllCars();

    CompletionStage<List<Car>> findAllCarsByBrand(String brand);

    CompletionStage<Done> insertOrUpdateCar(Car car);

    CompletionStage<Done> removeCarByGUID(GUID guid);

}
