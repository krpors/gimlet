package cruft.wtf.gimlet.event;

import cruft.wtf.gimlet.conf.Query;

/**
 * Event emitted when a {@link Query} was executed successfully.
 */
public class QueryExecutedEvent {
    private String query;

    private int rowCount;

    private Long runtime;

    public QueryExecutedEvent() {
    }

    public QueryExecutedEvent(final String query, final int rowCount, final Long runtime) {
        this.query = query;
        this.rowCount = rowCount;
        this.runtime = runtime;
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

    public Long getRuntime() {
        return runtime;
    }

    public void setRuntime(Long runtime) {
        this.runtime = runtime;
    }
}
