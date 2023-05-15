package cruft.wtf.gimlet.jdbc.task;

import cruft.wtf.gimlet.jdbc.SqlUtil;
import org.junit.jupiter.api.Test;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryTaskTest {

    @Test
    public void asdas() throws Exception {
        Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:querytasktest;shutdown=true");
        SqlUtil.runSql("/create.sql", c);

        QueryTask queryTask = new QueryTask(c, "select * from customer") {
            @Override
            public PreparedStatement prepareStatement() throws SQLException {
                return connection.prepareStatement(getQuery());
            }
        };

        CachedRowSet rowSet = queryTask.call();
        while (rowSet.next()) {
            System.out.println(rowSet.getString(2));
        }
        rowSet.release();
        rowSet.close();

        c.close();
    }
}