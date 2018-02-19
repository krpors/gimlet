package cruft.wtf.gimlet.ui.objects;

public class DatabaseObject {

    public static final int ROOT = 0;

    public static final int SCHEMA = 1;

    public static final int TABLE = 2;

    public static final int COLUMN = 3;

    private int type;

    private String name;

    public DatabaseObject(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
