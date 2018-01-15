package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.event.QueryExecuteEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This tab is added to the parent tab pane when an SQL connection is established via de Alias thing on the left hand
 * side of the application. The tab itself contains the actual SQL connection object, and in fact represents a session
 * to a data source. Once the tab is closed, the connection will be closed too.
 */
public class ConnectionTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ConnectionTab.class);

    private final Alias alias;

    private Connection connection;

    private TabPane tabPane = new TabPane();

    private ObjectsTab objectsTab;

    private SQLTab sqlTab;

    private DrillDownTab drillDownTab;

    public ConnectionTab(final Alias alias) throws SQLException {
        this.alias = alias;

        setGraphic(Images.BOLT.imageView());

        connection = DriverManager.getConnection(alias.getUrl(), alias.getUser(), alias.getPassword());
        logger.info("Connection successfully established for alias '{}'", alias.getName());

        setText(String.format("%s (%s) as %s", alias.getName(), connection.getCatalog(), alias.getUser()));

        setOnCloseRequest(e -> {
            try {
                // Close the connection when the tab is closed.
                connection.close();
                logger.info("Closed connection for '{}'", alias.getName());
                EventDispatcher.getInstance().unregister(this);
                logger.debug("Unregistered {} from Event Dispatcher", this);
            } catch (SQLException e1) {
                logger.error("Could not close connection ourselves", e1);
            }
        });


        setContent(createContent());
        EventDispatcher.getInstance().register(this);
    }

    /**
     * Main entry point for creating the content of the tab.
     *
     * @return The node containing the contents.
     */
    private Node createContent() {
        BorderPane pane = new BorderPane();

        FormPane topPaneWithLabels = new FormPane();

        Label lbl = new Label();
        lbl.getStyleClass().add("value-label");
        lbl.textProperty().bindBidirectional(alias.nameProperty());

        Label derp = new Label();
        derp.getStyleClass().add("value-label");
        derp.textProperty().bindBidirectional(alias.descriptionProperty());
        topPaneWithLabels.add("Name:", lbl);
        topPaneWithLabels.add("Description:", derp);

        pane.setTop(topPaneWithLabels);

        objectsTab = new ObjectsTab();
        sqlTab = new SQLTab(this);
        drillDownTab = new DrillDownTab(this);

        tabPane = new TabPane(objectsTab, sqlTab, drillDownTab);

        pane.setCenter(tabPane);

        return pane;
    }

    /**
     * Returns the SQL connection which (should have) has been established.
     *
     * @return The Connection to the data source.
     */
    public Connection getConnection() {
        return connection;
    }

    public void closeSelectedResultTable() {
        if (sqlTab.isSelected()) {
            sqlTab.closeSelectedResultTable();
        } else if (drillDownTab.isSelected()) {
            drillDownTab.closeSelectedResultTable();
        }
    }

    /**
     * When a {@link QueryExecuteEvent} is published on the bus, invoke this method to execute it on the drilldown tab.
     *
     * @param event The event.
     */
    @Subscribe
    public void onQueryExecute(final QueryExecuteEvent event) {
        // Make sure to apply the query to the current open/selected connection tab. Multiple ConnectionTabs can be
        // opened at once, so we make sure the query execution applies to the open tab.
        if (isSelected()) {
            logger.debug("Query will be executed on tab '{}' ({})", getText(), hashCode());
            tabPane.getSelectionModel().select(drillDownTab);
            drillDownTab.executeQuery(event.getQuery(), event.getColumnnMap());
        } else {
            logger.debug("Ignoring QueryExecuteEvent for tab '{}' ({}): tab is not selected", getText(), hashCode());
        }
    }
}
