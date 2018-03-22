package cruft.wtf.gimlet.jdbc;


import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A {@link NamedParameterPreparedStatement} is an implementation of {@link PreparedStatement} which allows us to make
 * use of named parameters. So instead of
 * <pre>    select * from customer where name = ? and lastname = ?</pre>
 * we can now use a format of
 * <pre>    select * from customer where name = :name and lastname = :lastname</pre>
 * which is pretty much required in Gimlet.
 * <p/>
 * This code is copied and altered from <a href="https://github.com/axiom-data-science/jdbc-named-parameters">axiom-data-science</a>.
 * <p/>
 * TODO: copyright notice (unlicense?)
 */
public class NamedParameterPreparedStatement extends DelegatingPreparedStatement {

    private final ParseResult parseResult;

    //factory methods for all possible PreparedStatement constructors
    public static NamedParameterPreparedStatement createNamedParameterPreparedStatement(
            Connection conn, String sql) throws SQLException {
        ParseResult parseResult = ParseResult.parse(sql);
        return new NamedParameterPreparedStatement(conn.prepareStatement(parseResult.getSql()), parseResult);
    }

    /**
     * Private constructor (use factory methods)
     *
     * @param delegate          PreparedStatement delegate
     * @param parseResult The parse result.
     */
    private NamedParameterPreparedStatement(PreparedStatement delegate, ParseResult parseResult) {
        super(delegate);
        this.parseResult = parseResult;
    }

    /**
     * Gets all defined parameters as an ordered set.
     *
     * @return The defined parameters in the SQL.
     */
    public Set<ParseResult.Param> getParameters() {
        return parseResult.getUniqueParameters();
    }

    private Collection<Integer> getParameterIndexes(String parameter) {
        Collection<Integer> indexes = new ArrayList<>();
        List<ParseResult.Param> orderedParameters = parseResult.getParameters();
        for (int i = 0; i < orderedParameters.size(); i++) {
            if (orderedParameters.get(i).getName().equals(parameter)) {
                //add i + 1, since all indexes ever are 0 based EXCEPT JDBC PARAMS WHYYYYY
                indexes.add(i + 1);
            }
        }

        return indexes;
    }

    public void setNull(String parameter, int sqlType) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setNull(i, sqlType);
        }
    }

    public void setBoolean(String parameter, boolean x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setBoolean(i, x);
        }
    }

    public void setByte(String parameter, byte x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setByte(i, x);
        }
    }

    public void setShort(String parameter, short x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setShort(i, x);
        }
    }

    public void setInt(String parameter, int x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setInt(i, x);
        }
    }

    public void setLong(String parameter, long x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setLong(i, x);
        }
    }

    public void setFloat(String parameter, float x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setFloat(i, x);
        }
    }

    public void setDouble(String parameter, float x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setDouble(i, x);
        }
    }

    public void setBigDecimal(String parameter, BigDecimal x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setBigDecimal(i, x);
        }
    }

    public void setString(String parameter, String x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setString(i, x);
        }
    }

    public void setBytes(String parameter, byte[] x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setBytes(i, x);
        }
    }

    public void setDate(String parameter, Date x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setDate(i, x);
        }
    }

    public void setTime(String parameter, Time x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setTime(i, x);
        }
    }

    public void setTimestamp(String parameter, Timestamp x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setTimestamp(i, x);
        }
    }

    public void setAsciiStream(String parameter, InputStream x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setAsciiStream(i, x);
        }
    }

    @Deprecated
    public void setUnicodeStream(String parameter, InputStream x, int length) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setUnicodeStream(i, x, length);
        }
    }

    public void setBinaryStream(String parameter, InputStream x, int length) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setBinaryStream(i, x, length);
        }
    }

    public void setObject(String parameter, Object x, int targetSqlType, int scale) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setObject(i, x, targetSqlType, scale);
        }
    }

    public void setObject(String parameter, Object x, int targetSqlType) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setObject(i, x, targetSqlType);
        }
    }

    public void setObject(String parameter, Object x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getDelegate().setObject(i, x);
        }
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }
}
