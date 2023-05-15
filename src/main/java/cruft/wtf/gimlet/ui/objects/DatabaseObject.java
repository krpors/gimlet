package cruft.wtf.gimlet.ui.objects;

public class DatabaseObject {

    public static final int ROOT = 0;

    public static final int SCHEMA = 1;

    public static final int TABLE = 2;

    public static final int COLUMN = 3;

    private int type;

    private String schema;

    private String table;

    public DatabaseObject(int type, String schema, String table) {
        this.type = type;
        this.schema = schema;
        this.table = table;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDisplayName() {
        if (table != null) {
            return table;
        } else if  (schema != null) {
            return schema;
        } else {
            return "!NO NAME!";
        }
    }
}
