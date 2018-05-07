package cruft.wtf.gimlet;

import cruft.wtf.gimlet.jdbc.SqlUtil;
import org.junit.Test;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestTask {

    @Test
    public void stuff() throws Exception {
        CachedRowSet rowSet = null;

        Connection c = DriverManager.getConnection("jdbc:hsqldb:mem");
        SqlUtil.runSql("/create.sql", c);
        rowSet = RowSetProvider.newFactory().createCachedRowSet();
        rowSet.setReadOnly(true);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("select * from customer");
        rowSet.populate(rs);
        rs.close();
        stmt.close();


        while (rowSet.next()) {
            System.out.println(rowSet.getString(2));
        }

        rowSet.restoreOriginal();

        while (rowSet.next()) {
            System.out.println(rowSet.getString(2));
        }

        rowSet.release();
        rowSet.close();
    }
}
