package cruft.wtf.gimlet.jdbc;

import cruft.wtf.gimlet.conf.Alias;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectTask extends Task<Connection> {

    private static Logger logger = LoggerFactory.getLogger(ConnectTask.class);

    private final Alias alias;

    private String password;

    public ConnectTask(final Alias aliasToConnectTo) {
        this.alias = aliasToConnectTo;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected Connection call() throws Exception {
        Class.forName(alias.getDriverClass());

        Connection c = DriverManager.getConnection(alias.getUrl(), alias.getUser(), password);
        logger.info("Connection successfully established to {}", alias.getUrl());
        return c;
    }
}
