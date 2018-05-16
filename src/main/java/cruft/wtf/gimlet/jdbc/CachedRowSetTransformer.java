package cruft.wtf.gimlet.jdbc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public final class CachedRowSetTransformer {

    private static Logger logger = LoggerFactory.getLogger(CachedRowSetTransformer.class);

    public static List<Column> getColumns(final CachedRowSet rowSet) throws SQLException {
        rowSet.restoreOriginal();

        List<Column> columnList = new LinkedList<>();
        ResultSetMetaData rsmd = rowSet.getMetaData();
        logger.debug("Found {} columns in ResultSet", rsmd.getColumnCount());
        for (int col = 0; col < rsmd.getColumnCount(); col++) {
            int z = col + 1;
            Column column = new Column(rsmd.getColumnType(z), rsmd.getColumnName(z));
            columnList.add(column);
        }
        return columnList;
    }

    public static ObservableList<ObservableList> getData(final CachedRowSet rowSet) throws SQLException {
        rowSet.restoreOriginal();

        ResultSetMetaData rsmd = rowSet.getMetaData();

        ObservableList<ObservableList> tempList = FXCollections.observableArrayList();
        while (rowSet.next()) {
            ObservableList<Object> list = FXCollections.observableArrayList();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                // Just add it as an Object. This also retains nullable numeric values for instance.
                // If we do explicit gets, for ex. rs.getLong(), when the column is effectively NULL,
                // the list will add a '0' instead. This will not retain NULL.
                list.add(rowSet.getObject(i));
            }

            tempList.add(list);
        }
        return tempList;
    }
}
