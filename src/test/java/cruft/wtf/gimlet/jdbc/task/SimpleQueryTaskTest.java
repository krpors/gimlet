package cruft.wtf.gimlet.jdbc.task;

import cruft.wtf.gimlet.jdbc.SqlUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

public class SimpleQueryTaskTest {

    private static Connection connection;

    @BeforeAll
    public static void setup() throws SQLException {
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:simplequerytasktest;shutdown=true");
        System.out.println("Created connection");
    }

    @AfterAll
    public static void teardown() throws SQLException {
        connection.close();
        System.out.println("Closed connection");
    }

    @Test
    public void test() throws Exception {
        List<String> queries = SqlUtil.parseQueries("/create.sql");
        for (String q : queries) {
            Statement stmt = connection.createStatement();
            stmt.executeQuery(q);
            stmt.close();
            System.out.println("Executed query " + q);
        }

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from customer");
        while (rs.next()) {
            System.out.println(rs.getString(2));
        }

        rs.close();
    }

    @Test
    public void herro() {
    }
}
