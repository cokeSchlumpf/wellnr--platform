package samples.data.car;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.guid.HasGUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.mongojack.ObjectId;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Car implements HasGUID {

    private static final String ID = "_id";
    private static final String BRAND = "brand";
    private static final String COLOR = "color";
    private static final String ENGINE = "engine";
    private static final String DRIVERS = "drivers";

    @ObjectId
    @JsonProperty(ID)
    com.wellnr.platform.common.guid.GUID guid;

    @JsonProperty(BRAND)
    String brand;

    @JsonProperty(COLOR)
    String color;

    @JsonProperty(ENGINE)
    Engine engine;

    @JsonProperty(DRIVERS)
    List<Driver> drivers;

    @JsonCreator
    public static Car apply(
        @ObjectId @JsonProperty(ID) GUID guid,
        @JsonProperty(BRAND) String brand,
        @JsonProperty(COLOR) String color,
        @JsonProperty(ENGINE) Engine engine,
        @JsonProperty(DRIVERS) List<Driver> drivers
    ) {
        return new Car(guid, brand, color, engine, drivers);
    }

    public static Car apply(
        @JsonProperty(BRAND) String brand,
        @JsonProperty(COLOR) String color,
        @JsonProperty(ENGINE) Engine engine,
        @JsonProperty(DRIVERS) List<Driver> drivers
    ) {
        return new Car(GUID.apply(brand), brand, color, engine, drivers);
    }

    public GUID getGUID() {
        return this.guid;
    }
}
