package cruft.wtf.gimlet.jdbc.task;

import cruft.wtf.gimlet.jdbc.NamedParameterPreparedStatement;
import cruft.wtf.gimlet.jdbc.ParseResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * A JavaFX task to run a named parameter query.
 */
public class NamedQueryTask extends QueryTask {

    private Map<String, Object> namedProperties;

    public NamedQueryTask(final Connection connection, final String query, int maxRows, Map<String, Object> props) {
        super(connection, query, maxRows);
        this.namedProperties = props;
    }

    public Map<String, Object> getNamedProperties() {
        return namedProperties;
    }

    @Override
    public PreparedStatement prepareStatement() throws SQLException {
        NamedParameterPreparedStatement statement =
                NamedParameterPreparedStatement.createNamedParameterPreparedStatement(this.connection, this.query);
        for (ParseResult.Param paramName : statement.getParameters()) {
            Object value = namedProperties.get(paramName.getName());
            if (value == null) {
                throw new SQLException(String.format("The predefined statement wants a value for %s, but none found in parameter map.", paramName));
            }

            statement.setObject(paramName.getName(), value);
        }
        return statement;
    }
}
