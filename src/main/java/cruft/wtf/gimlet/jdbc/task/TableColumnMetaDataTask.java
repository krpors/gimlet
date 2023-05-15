package cruft.wtf.gimlet.jdbc.task;

import cruft.wtf.gimlet.jdbc.SqlType;
import cruft.wtf.gimlet.ui.objects.DatabaseObject;
import cruft.wtf.gimlet.ui.objects.ObjectsTableData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * A JavaFX task to get metadata (+ sample data) from a table. This task is used by the
 * {@link cruft.wtf.gimlet.ui.objects.ObjectsTab}.
 */
public class TableColumnMetaDataTask extends Task<ObservableList<ObjectsTableData>> {

    private static final Logger log = LoggerFactory.getLogger(TableColumnMetaDataTask.class);

    private final Connection connection;

    private final String schemaName;

    private final DatabaseObject dbObject;

    private final int maxRows;

    public TableColumnMetaDataTask(final Connection connection, final String schemaName, final DatabaseObject dbObject, int maxRows) {
        this.connection = connection;
        this.schemaName = schemaName;
        this.dbObject = dbObject;
        this.maxRows = maxRows;
    }

    @Override
    protected ObservableList<ObjectsTableData> call() throws Exception {
        if (dbObject.getType() != DatabaseObject.TABLE) {
            // dbObject is not of type TABLE:
            throw new IllegalArgumentException("The supplied dbObject must be of type TABLE, but was " + dbObject.getType());
        }

        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet rs = dmd.getColumns(null, schemaName, dbObject.getTable(), "%");

        // Get primary keys of the table.
        ResultSet pks = dmd.getPrimaryKeys(null, schemaName, dbObject.getTable());
        Set<String> setColumnPks = new HashSet<>();
        while (pks.next()) {
            setColumnPks.add(pks.getString("COLUMN_NAME"));
        }
        pks.close();

        ObservableList<ObjectsTableData> derp = FXCollections.observableArrayList();

        int ordinal = 0;
        while (rs.next()) {
            ObjectsTableData data = new ObjectsTableData();
            data.setOrdinalPosition(ordinal++);
            data.setColumnName(rs.getString("COLUMN_NAME"));
            data.setDataType(SqlType.getType(rs.getInt("DATA_TYPE")));
            data.setTypeName(rs.getString("TYPE_NAME"));
            data.setColumnSize(rs.getInt("COLUMN_SIZE"));
            data.setNullable(rs.getInt("NULLABLE") != 0);
            data.setRemarks(rs.getString("REMARKS"));
            data.setPrimaryKey(setColumnPks.contains(rs.getString("COLUMN_NAME")));
            derp.add(data);
        }
        rs.close();

        return derp;
    }
}
