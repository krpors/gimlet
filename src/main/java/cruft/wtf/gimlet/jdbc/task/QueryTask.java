package cruft.wtf.gimlet.jdbc.task;

import cruft.wtf.gimlet.Column;
import cruft.wtf.gimlet.Utils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * A JavaFX task to run a simple query (i.e. a typed query, without named parameters).
 */
public abstract class QueryTask extends Task<ObservableList<ObservableList>> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private SimpleIntegerProperty rowCount = new SimpleIntegerProperty(0);

    private SimpleLongProperty processingTime = new SimpleLongProperty(0);

    private ObservableList<Column> columns = FXCollections.observableArrayList();

    protected final Connection connection;

    protected final String query;

    protected int maxRows;

    public QueryTask(final Connection connection, final String query) {
        this(connection, query, 0);
    }

    public QueryTask(final Connection connection, final String query, int maxRows) {
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

    /**
     * This method prepares the proper statement for execution in the {@link #call()} method.
     *
     * @return The PreparedStatement.
     */
    public abstract PreparedStatement prepareStatement() throws SQLException;

    /**
     * Executes the task, and returns the rowdata which is returned by the query. This rowdata is given as
     * a list of lists. The initial list contains the rows, whereas the contained list is a list with columns.
     *
     * @return The row data with column data per row.
     * @throws Exception Whenever.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected ObservableList<ObservableList> call() throws Exception {
        logger.debug("Running task");
        long before = System.currentTimeMillis();

        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            // TODO: make the statement cancellable from another thread.
            // When a huge query is run, you're unable to cancel it.
            logger.debug("Preparing statement. Setting max rows to {}", maxRows);
            statement = prepareStatement();
            statement.setMaxRows(maxRows);
            rs = statement.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            logger.debug("Found {} columns in ResultSet", rsmd.getColumnCount());
            for (int col = 0; col < rsmd.getColumnCount(); col++) {
                int z = col + 1;
                Column column = new Column(rsmd.getColumnType(z), rsmd.getColumnName(z));
                columns.add(column);
            }

            ObservableList<ObservableList> tempList = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<Object> list = FXCollections.observableArrayList();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    // Based on the column type, add a different type of data (int, long, string, etc).
                    switch (rsmd.getColumnType(i)) {
                        case Types.BIGINT:
                            list.add(rs.getLong(i));
                            break;
                        case Types.NUMERIC:
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
                            break;
                        default:
                            list.add(rs.getString(i));
                    }
                }

                tempList.add(list);
                rowCount.set(rowCount.get() + 1);
            }

            // When all is finished, add it to the end list. This will notify listeners.
            processingTime.set(System.currentTimeMillis() - before);
            logger.debug("Task finished in {} ms, resulting in {} rows", processingTime.get(), rowCount);
            return tempList;
        } catch (SQLException ex) {
            logger.error("SQL Exception", ex);
            throw ex;
        } finally {
            Utils.close(statement);
            logger.debug("Closed statement");
            Utils.close(rs);
            logger.debug("Closed result set");
        }
    }
}
