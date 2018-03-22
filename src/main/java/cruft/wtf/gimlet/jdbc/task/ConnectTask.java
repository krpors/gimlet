package cruft.wtf.gimlet.jdbc.task;

import cruft.wtf.gimlet.conf.Alias;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectTask extends Task<Connection> {

    private static Logger logger = LoggerFactory.getLogger(ConnectTask.class);

    private final Alias alias;

    private String password;

    public ConnectTask(final Alias aliasToConnectTo) {
        this.alias = aliasToConnectTo;
        setPassword(aliasToConnectTo.getPassword());
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected Connection call() throws Exception {
        try {
            Class.forName(alias.getDriverClass());
        } catch (ClassNotFoundException ex) {
            logger.error("JDCB driver class not found", ex);
            throw ex;
        }

        try {
            Connection c = DriverManager.getConnection(alias.getUrl(), alias.getUser(), password);
            logger.info("Connection successfully established to {}", alias.getUrl());
            return c;
        } catch (SQLException ex) {
            logger.error("Could not establish JDBC connection for alias " +  alias.getName(), ex);
            throw ex;
        }
    }
}
