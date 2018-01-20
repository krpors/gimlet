package cruft.wtf.gimlet.jdbc;

import cruft.wtf.gimlet.conf.Alias;
import javafx.concurrent.Task;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

public class ConnectTask extends Task<Connection> {

    private static Logger logger = LoggerFactory.getLogger(ConnectTask.class);

    private final Alias alias;

    public ConnectTask(final Alias aliasToConnectTo) {
        this.alias = aliasToConnectTo;
    }

    @Override
    protected Connection call() throws Exception {
        Class.forName(alias.getDriverClass());

        String password = alias.getPassword();
        if (alias.isAskForPassword()) {
            // TODO: password input dialog.
            TextInputDialog dlg = new TextInputDialog("");
            dlg.setHeaderText("Specify password for user '" + alias.getUser() + "'");
            Optional<String> pwd = dlg.showAndWait();
            if (pwd.isPresent()) {
                password = pwd.get();
            }
        }

        Connection c = DriverManager.getConnection(alias.getUrl(), alias.getUser(), password);
        logger.info("Connection successfully established to {}", alias.getUrl());
        return c;
    }
}
