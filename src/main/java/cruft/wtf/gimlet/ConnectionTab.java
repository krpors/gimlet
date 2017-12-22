package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class ConnectionTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ConnectionTab.class);

    private final Alias alias;

    private Connection connection;

    public ConnectionTab(final Alias alias) throws SQLException {
        this.alias = alias;

        setGraphic(Images.BOLT.imageView());
        setText(alias.getName());

        connection = DriverManager.getConnection(alias.getUrl(), alias.getUser(), alias.getPassword());
        logger.info("Connection successfully established for alias '{}'", alias.getName());

        setOnCloseRequest(e -> {
            try {
                connection.close();
                logger.info("Closed connection for '{}'", alias.getName());
            } catch (SQLException e1) {
                logger.error("Could not close connection ourselves", e1);
            }
        });

        QueryTree tree = new QueryTree();
        tree.setQueryList(GimletApp.gimletProject.queriesProperty());

        setContent(tree);
    }

}
