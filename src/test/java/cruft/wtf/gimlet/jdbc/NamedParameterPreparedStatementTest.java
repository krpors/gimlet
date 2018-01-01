package cruft.wtf.gimlet.jdbc;


import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class NamedParameterPreparedStatementTest {

    @Test
    public void asda() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/", "SA", "");
        NamedParameterPreparedStatement npsm =
                NamedParameterPreparedStatement.createNamedParameterPreparedStatement(c, "select * from customer where firstname = :firstname and id = :id");
        npsm.getParameters().forEach(System.out::println);
        npsm.setString("firstname", "Susan");
        npsm.setString("id", "4aa");

        System.out.println(npsm.toString());

        c.close();
    }
}
