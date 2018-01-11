package cruft.wtf.gimlet;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * A JavaFX task to run a simple query (i.e. a typed query, without named parameters).
 */
public class SimpleQueryTask extends Task<ObservableList<ObservableList>> {

    private static Logger logger = LoggerFactory.getLogger(SimpleQueryTask.class);

    private SimpleIntegerProperty rowCount = new SimpleIntegerProperty(0);

    private SimpleLongProperty processingTime = new SimpleLongProperty(0);

    private ObservableList<Column> columns = FXCollections.observableArrayList();

    private final Connection connection;

    private final String query;

    private int maxRows;

    public SimpleQueryTask(final Connection connection, final String query, int maxRows) {
        this.connection = connection;
        this.query = query;
        this.maxRows = maxRows;
    }

    public int getRowCount() {
        return rowCount.get();
    }

    public SimpleIntegerProperty rowCountProperty() {
        return rowCount;
    }

    public long getProcessingTime() {
        return processingTime.get();
    }

    public SimpleLongProperty processingTimeProperty() {
        return processingTime;
    }

    public ObservableList<Column> columnProperty() {
        return columns;
    }

    public String getQuery() {
        return query;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ObservableList<ObservableList> call() throws Exception {
        logger.debug("Running task");
        long before = System.currentTimeMillis();

        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setMaxRows(maxRows);
            rs = statement.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            for (int col = 0; col < rsmd.getColumnCount(); col++) {
                int z = col + 1;
                Column column = new Column(rsmd.getColumnType(z), rsmd.getColumnName(z));
                columns.add(column);
            }

            ObservableList<ObservableList> tempList = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<Object> list = FXCollections.observableArrayList();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    // first element is the actual row number, which is just the i counter;

                    // Based on the column type, add a different type of data (int, long, string, etc).
                    switch (rsmd.getColumnType(i)) {
                        case Types.BIGINT:

                            list.add(rs.getLong(i));
                            break;
                        case Types.SMALLINT:
                        case Types.TINYINT:
                        case Types.INTEGER:
                            list.add(rs.getInt(i));
                            break;
                        case Types.CHAR:
                        case Types.LONGNVARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.VARCHAR:
                            list.add(rs.getString(i));
                            break;
                        case Types.TIMESTAMP:
                        case Types.TIMESTAMP_WITH_TIMEZONE:
                            list.add(rs.getTimestamp(i));
                            break;
                        case Types.DATE:
                            list.add(rs.getDate(i));
                            break;
                        case Types.TIME:
                            list.add(rs.getTime(i));
                        default:
                            list.add(rs.getString(i));
                    }
                }

                tempList.add(list);
                rowCount.set(rowCount.get() + 1);
            }

            // When all is finished, add it to the end list. This will notify listeners.
            processingTime.set(System.currentTimeMillis() - before);

            logger.debug("Finished task.");
            return tempList;
        } finally {
            Utils.close(statement);
            logger.debug("Closed statement");
            Utils.close(rs);
            logger.debug("Closed result set");
        }
    }
}
