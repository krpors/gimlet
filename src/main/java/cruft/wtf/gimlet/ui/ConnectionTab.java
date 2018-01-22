package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.event.QueryExecuteEvent;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

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

    /**
     * The content pane with the tabs etc for the connection.
     */
    private BorderPane contentPane;

    /**
     * Timer to run to give an indication how long we've been trying to connect.
     */
    private Timer connectionTimer;

    private TextArea txtError;

    /**
     * Stackpane, to either make the label visible (with connect time), or the actual content pane.
     */
    private StackPane stackPane;

    private Label lblConnectionTime;

    public ConnectionTab(final Alias alias) {
        this.alias = alias;
        setGraphic(Images.CLOCK.imageView());

        EventDispatcher.getInstance().register(this);

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (alias.isColorDisabled()) {
                return;
            }

            if (newValue) {
                setStyle("-fx-background-color: linear-gradient(to top, " + alias.getColor() + ", -fx-background)");
            } else {
                setStyle(null);
            }
        });

        setText(String.format("%s as %s", alias.getName(), alias.getUser()));

        setOnCloseRequest(e -> {
            EventDispatcher.getInstance().unregister(this);
            connectionTimer.cancel();
            try {
                // Close the connection when the tab is closed.
                if (connection != null) {
                    connection.close();
                    logger.info("Closed connection for '{}'", alias.getName());
                    logger.debug("Unregistered {} from Event Dispatcher", this);
                }
            } catch (SQLException e1) {
                logger.error("Could not close connection ourselves", e1);
            }
        });

        setContent(createContent());
    }

    /**
     * Creates and runs the counter task to see how long we've been trying to connect.
     */
    public void startTimer() {
        connectionTimer = new Timer(false);
        connectionTimer.scheduleAtFixedRate(new TimerTask() {
            private long seconds = 0;

            @Override
            public void run() {
                Platform.runLater(() -> lblConnectionTime.setText("Trying to connect for " + seconds++ + " seconds"));
            }
        }, 0, 1000);
    }

    /**
     * If we could connect via the {@link cruft.wtf.gimlet.jdbc.ConnectTask}, the connection will be assigned to this
     * tab. The graphic is changed, the connectionTimer is cancelled, and the content pane is made visible.
     *
     * @param connection The connection to set.
     */
    public void setConnection(final Connection connection) {
        logger.debug("Established connection in connection tab");
        this.connection = connection;
        setGraphic(Images.BOLT.imageView());
        // Cancel the counter connectionTimer, we're done.
        connectionTimer.cancel();
        // The visible part of this tab is now the normal border pane.
        contentPane.setVisible(true);
        lblConnectionTime.setVisible(false);

        objectsTab.setConnection(connection);
//        objectsTab.doit();
    }

    public void setThrowable(final Throwable throwable) {
        connectionTimer.cancel();
        stackPane.getChildren().forEach(node -> {
            node.setVisible(false);
        });

        txtError.setVisible(true);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        StringBuilder err = new StringBuilder();
        err
                .append("Unable to connect\n\n")
                .append("Username: ").append(alias.getUser()).append("\n")
                .append("URL:      ").append(alias.getUrl()).append("\n")
                .append("Driver:   ").append(alias.getDriverClass()).append("\n\n")
                .append("Stacktrace:\n\n")
                .append(sw.toString());

        txtError.setText(err.toString());
    }

    /**
     * Main entry point for creating the content of the tab.
     *
     * @return The node containing the contents.
     */
    private Node createContent() {
        lblConnectionTime = new Label();

        // The content pane (which holds everything) is initially invisible when we're trying to connect.
        contentPane = new BorderPane();
        contentPane.setVisible(false);

        FormPane topPaneWithLabels = new FormPane();

        Label lbl = new Label();
        lbl.getStyleClass().add("value-label");
        lbl.textProperty().bindBidirectional(alias.nameProperty());

        Label derp = new Label();
        derp.getStyleClass().add("value-label");
        derp.textProperty().bindBidirectional(alias.descriptionProperty());
        topPaneWithLabels.add("Name:", lbl);
        topPaneWithLabels.add("Description:", derp);

        contentPane.setTop(topPaneWithLabels);

        objectsTab = new ObjectsTab();
        sqlTab = new SQLTab(this);
        drillDownTab = new DrillDownTab(this);

        tabPane = new TabPane(objectsTab, sqlTab, drillDownTab);

        contentPane.setCenter(tabPane);

        // Create the textarea for errors.
        txtError = new TextArea();
        txtError.setEditable(false);
        txtError.getStyleClass().add("textarea");
        txtError.setVisible(false);

        // Stackpane with stacked items which may or may not be visible.
        stackPane = new StackPane();
        stackPane.getChildren().add(lblConnectionTime);
        stackPane.getChildren().add(contentPane);
        stackPane.getChildren().add(txtError);
        return stackPane;
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
