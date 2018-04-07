package cruft.wtf.gimlet.jdbc;

import cruft.wtf.gimlet.jdbc.task.SimpleQueryTask;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * EXTREMELY simple class to parse SQL files into separate statements. I don't need the overhead
 * of DbUnit (yet...?) so it's just basic reading files, separate statements by semicolon ({@code ;}),
 * disregarding the context where the semicolon is used. Could have used a regex or String.split instead but meh.
 */
public final class SqlUtil {

    public static List<String> parseQueries(String resource) throws IOException {
        InputStream is = SimpleQueryTask.class.getResourceAsStream(resource);
        StringBuilder sb = new StringBuilder();

        List<String> queryList = new LinkedList<>();

        int i;
        while ((i = is.read()) != -1) {
            if (i == ';') {
                queryList.add(sb.toString().trim());
                sb = new StringBuilder();
                continue;
            }
            // skip these whitespaces, make them spaces instead.
            if (i == '\n' || i == '\r') {
                sb.append(' ');
                continue;
            }

            sb.append((char) i);
        }

        is.close();

        return queryList;
    }

    public static void runSql(final String resource, final Connection connection) throws IOException, SQLException {
        Objects.requireNonNull(connection);

        List<String> statements = parseQueries(resource);
        for (String s : statements) {
            Statement stmt = connection.createStatement();
            stmt.execute(s);
            System.out.printf("Executed query: %s\n", s);
            stmt.close();
        }
    }
}
