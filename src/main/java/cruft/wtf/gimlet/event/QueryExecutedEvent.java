package cruft.wtf.gimlet.event;

import cruft.wtf.gimlet.conf.Query;

/**
 * Event emitted when a {@link Query} was executed successfully.
 */
public class QueryExecutedEvent {
    private String query;

    private int rowCount;

    public QueryExecutedEvent() {

    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
}
