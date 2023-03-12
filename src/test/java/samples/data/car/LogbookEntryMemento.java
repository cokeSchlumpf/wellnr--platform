package samples.data.car;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.guid.HasGUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogbookEntryMemento implements HasGUID {

    private static final String ID = "_id";
    private static final String CAR = "car";
    private static final String FROM = "from";
    private static final String TO = "to";

    @JsonProperty(ID)
    GUID guid;

    @JsonProperty(CAR)
    GUID car;

    @JsonProperty(FROM)
    String from;

    @JsonProperty(TO)
    String to;

    @JsonCreator
    public static LogbookEntryMemento apply(
        @JsonProperty(ID) GUID guid,
        @JsonProperty(CAR) GUID car,
        @JsonProperty(FROM) String from,
        @JsonProperty(TO) String to
    ) {
        return new LogbookEntryMemento(guid, car, from, to);
    }

    @Override
    public GUID getGUID() {
        return guid;
    }
}
