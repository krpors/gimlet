package cruft.wtf.gimlet.event;

import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.ui.QueryEditDialog;

/**
 * Event emitted when a {@link cruft.wtf.gimlet.conf.Query} was saved via the {@link QueryEditDialog}.
 */
public class QuerySavedEvent {
    private Query query;

    public QuerySavedEvent(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }
}
