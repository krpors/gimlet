package cruft.wtf.gimlet.event;

import cruft.wtf.gimlet.conf.Query;

/**
 * Event emitted when a {@link cruft.wtf.gimlet.conf.Query} is being edited.
 */
public class QueryEditEvent {
    private Query query;

    public QueryEditEvent(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }
}
