package cruft.wtf.gimlet.event;

import cruft.wtf.gimlet.conf.Query;

import java.util.Map;
import java.util.TreeMap;

public class QueryExecuteEvent {

    private Query query;

    private Map<String, Object> columnnMap = new TreeMap<>();

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Map<String, Object> getColumnnMap() {
        return columnnMap;
    }

    public void setColumnnMap(Map<String, Object> columnnMap) {
        this.columnnMap = columnnMap;
    }
}
