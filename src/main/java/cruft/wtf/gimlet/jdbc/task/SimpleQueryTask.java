package cruft.wtf.gimlet.jdbc.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A JavaFX task to run a simple query (i.e. a typed query, without named parameters).
 */
public class SimpleQueryTask extends QueryTask {

    public SimpleQueryTask(final Connection connection, final String query, int maxRows) {
        super(connection, query, maxRows);
    }

    @Override
    public PreparedStatement prepareStatement() throws SQLException {
        return this.connection.prepareStatement(query);
    }
}
