package samples.data.car;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.persistence.memento.HasMemento;
import com.wellnr.platform.core.context.PlatformContext;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class LogbookEntry implements HasMemento<LogbookEntryMemento> {

    GUID guid;

    Car car;

    String from;

    String to;

    public static LogbookEntry createFromMemento(PlatformContext ctx, LogbookEntryMemento memento) {
        var cars = ctx.getInstance(CarsRepository.class);
        var car = cars.getCarByGUID(memento.getCar());

        return LogbookEntry.apply(
            memento.getGUID(), car, memento.getFrom(), memento.getTo()
        );
    }

    @Override
    public LogbookEntryMemento getMemento() {
        return LogbookEntryMemento.apply(guid, car.getGUID(), from, to);
    }

    @Override
    public GUID getGUID() {
        return guid;
    }

}
