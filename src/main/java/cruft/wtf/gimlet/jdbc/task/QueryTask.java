package cruft.wtf.gimlet.jdbc.task;

import com.sun.rowset.CachedRowSetImpl;
import cruft.wtf.gimlet.Utils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A JavaFX task to run a simple query (i.e. a typed query, without named parameters).
 */
public abstract class QueryTask extends Task<CachedRowSet> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private SimpleIntegerProperty rowCount = new SimpleIntegerProperty(0);

    private SimpleLongProperty processingTime = new SimpleLongProperty(0);

    private CachedRowSet cachedRowSet;

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

    public String getQuery() {
        return query;
    }

    public CachedRowSet getCachedRowSet() {
        return cachedRowSet;
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
    protected CachedRowSet call() throws Exception {
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

            cachedRowSet = new CachedRowSetImpl();
            cachedRowSet.populate(rs);
            rowCount.set(cachedRowSet.size());

            // When all is finished, add it to the end list. This will notify listeners.
            processingTime.set(System.currentTimeMillis() - before);
            logger.debug("Task finished in {} ms, resulting in {} rows in {} columns",
                    processingTime.get(), rowCount.get(), cachedRowSet.getMetaData().getColumnCount());
            return cachedRowSet;
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
