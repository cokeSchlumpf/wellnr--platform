package samples.data.car;

import com.wellnr.platform.common.guid.GUID;

import java.util.List;
import java.util.Optional;

public interface CarsRepository {

    Optional<Car> findOneCarByGUID(GUID guid);

    default Car getCarByGUID(GUID guid) {
        return findOneCarByGUID(guid).orElseThrow();
    }

    List<Car> findAllCars();

    List<Car> findAllCarsByBrand(String brand);

    void insertOrUpdateCar(Car car);

    void removeCarByGUID(GUID guid);

}
