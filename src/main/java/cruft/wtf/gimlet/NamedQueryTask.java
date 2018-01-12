package cruft.wtf.gimlet;

import cruft.wtf.gimlet.jdbc.NamedParameterPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * A JavaFX task to run a named parameter query.
 */
public class NamedQueryTask extends QueryTask {

    private Map<String, Object> namedProperties = new TreeMap<>();

    public NamedQueryTask(final Connection connection, final String query, int maxRows, Map<String, Object> props) {
        super(connection, query, maxRows);
        this.namedProperties = props;
    }


    @Override
    public PreparedStatement prepareStatement() throws SQLException {
        NamedParameterPreparedStatement statement =
                NamedParameterPreparedStatement.createNamedParameterPreparedStatement(this.connection, this.query);
        for (String paramName : statement.getParameters()) {
            Object value = namedProperties.get(paramName);
            if (value == null) {
                throw new SQLException(String.format("The predefined statement wants a value for %s, but none found in parameter map.", paramName));
            }

            statement.setObject(paramName, value);
        }
        return statement;
    }
}
