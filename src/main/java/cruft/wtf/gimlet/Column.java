package cruft.wtf.gimlet;

public class Column {
    private final int type;
    private final String columnName;

    public Column(int type, String columnName) {
        this.type = type;
        this.columnName = columnName;
    }

    public int getType() {
        return type;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public String toString() {
        return "Column{" +
                "type=" + type +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}
