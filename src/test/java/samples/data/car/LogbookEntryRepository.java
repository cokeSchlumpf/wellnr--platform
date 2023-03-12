package samples.data.car;

import java.util.List;

public interface LogbookEntryRepository {

    void insertOrUpdateLogbookEntry(LogbookEntry entry);

    List<LogbookEntry> findAllLogbookEntriesByFrom(String from);

}
