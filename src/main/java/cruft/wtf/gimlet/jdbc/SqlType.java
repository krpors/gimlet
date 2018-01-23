package cruft.wtf.gimlet.jdbc;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class SqlType {

    private static Map<Integer, String> integerStringMap = new HashMap<>();

    static {
        integerStringMap.put(Types.BIT, "BIT");
        integerStringMap.put(Types.TINYINT, "TINYINT");
        integerStringMap.put(Types.SMALLINT, "SMALLINT");
        integerStringMap.put(Types.INTEGER, "INTEGER");
        integerStringMap.put(Types.BIGINT, "BIGINT");
        integerStringMap.put(Types.FLOAT, "FLOAT");
        integerStringMap.put(Types.REAL, "REAL");
        integerStringMap.put(Types.DOUBLE, "DOUBLE");
        integerStringMap.put(Types.NUMERIC, "NUMERIC");
        integerStringMap.put(Types.DECIMAL, "DECIMAL");
        integerStringMap.put(Types.CHAR, "CHAR");
        integerStringMap.put(Types.VARCHAR, "VARCHAR");
        integerStringMap.put(Types.LONGVARCHAR, "LONGVARCHAR");
        integerStringMap.put(Types.DATE, "DATE");
        integerStringMap.put(Types.TIME, "TIME");
        integerStringMap.put(Types.TIMESTAMP, "TIMESTAMP");
        integerStringMap.put(Types.BINARY, "BINARY");
        integerStringMap.put(Types.VARBINARY, "VARBINARY");
        integerStringMap.put(Types.LONGVARBINARY, "LONGVARBINARY");
        integerStringMap.put(Types.NULL, "NULL");
        integerStringMap.put(Types.OTHER, "OTHER");
        integerStringMap.put(Types.JAVA_OBJECT, "JAVA_OBJECT");
        integerStringMap.put(Types.DISTINCT, "DISTINCT");
        integerStringMap.put(Types.STRUCT, "STRUCT");
        integerStringMap.put(Types.ARRAY, "ARRAY");
        integerStringMap.put(Types.BLOB, "BLOB");
        integerStringMap.put(Types.CLOB, "CLOB");
        integerStringMap.put(Types.REF, "REF");
        integerStringMap.put(Types.DATALINK, "DATALINK");
        integerStringMap.put(Types.BOOLEAN, "BOOLEAN");
        integerStringMap.put(Types.ROWID, "ROWID");
        integerStringMap.put(Types.NCHAR, "NCHAR");
        integerStringMap.put(Types.NVARCHAR, "NVARCHAR");
        integerStringMap.put(Types.LONGNVARCHAR, "LONGNVARCHAR");
        integerStringMap.put(Types.NCLOB, "NCLOB");
        integerStringMap.put(Types.SQLXML, "SQLXML");
        integerStringMap.put(Types.REF_CURSOR, "REF_CURSOR");
        integerStringMap.put(Types.TIME_WITH_TIMEZONE, "TIME_WITH_TIMEZONE");
        integerStringMap.put(Types.TIMESTAMP_WITH_TIMEZONE, "TIMESTAMP_WITH_TIMEZONE");
    }

    public static String getType(int i) {
        return integerStringMap.get(i);
    }
}

