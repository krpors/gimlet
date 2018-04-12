package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.event.ConnectEvent;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.QueryExecuteEvent;
import cruft.wtf.gimlet.jdbc.task.ConnectTask;
import cruft.wtf.gimlet.jdbc.task.ConnectionValidityTimer;
import cruft.wtf.gimlet.ui.controls.NumberTextField;
import cruft.wtf.gimlet.ui.drilldown.DrillDownTab;
import cruft.wtf.gimlet.ui.objects.ObjectsTab;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
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

    private CheckBox chkLimitRows;

    private NumberTextField numMaxRowCount;

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

    private ConnectionValidityTimer connectionValidityTimer;

    private TextArea txtError;

    /**
     * Stackpane, to either make the label visible (with connect time), or the actual content pane.
     */
    private StackPane stackPane;

    // TODO: progress indicator during connecting.
    private ProgressIndicator progressIndicator;

    private Label lblConnectionTime;

    /**
     * Creates the {@link ConnectionTab} using the given {@link Alias}.
     *
     * @param alias The alias containing the connection information.
     */
    public ConnectionTab(final Alias alias) {
        this.alias = Objects.requireNonNull(alias);

        setGraphic(Images.CLOCK.imageView());

        EventDispatcher.getInstance().register(this);

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!alias.isColorDisabled() && newValue) {
                setStyle("-fx-base: " + alias.getColor());
            } else {
                setStyle(null);
            }
        });

        // No two-way binding, since the user might be changed. This would reflect
        // an untrue situation so keep this tab name static.
        setText(String.format("%s as %s", alias.getName(), alias.getUser()));

        setOnCloseRequest(e -> {
            EventDispatcher.getInstance().unregister(this);
            connectionTimer.cancel();
            connectionValidityTimer.cancel();
            try {
                // Close the connection when the tab is closed.
                if (connection != null) {
                    connection.close();
                    EventDispatcher.getInstance().post(new ConnectEvent(ConnectEvent.Type.CLOSED, alias));
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
     * Returns the Alias which was
     *
     * @return
     */
    public Alias getAlias() {
        return alias;
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
     * If we could connect via the {@link ConnectTask}, the connection will be assigned to this
     * tab. The graphic is changed, the connectionTimer is cancelled, and the content pane is made visible.
     *
     * We're also starting a timer task in the background to continuously check whether we still have a connection.
     *
     * @param connection The connection to set.
     */
    public void setConnection(final Connection connection) {
        logger.debug("Established connection in connection tab");
        this.connection = connection;
        setGraphic(Images.ACCOUNT_LOGIN.imageView());
        // Cancel the counter connectionTimer, we're done.
        connectionTimer.cancel();
        // The visible part of this tab is now the normal border pane.
        contentPane.setVisible(true);
        lblConnectionTime.setVisible(false);

        objectsTab.setConnection(connection);

        startConnectionValidityChecker();
    }

    /**
     * This method will start a timer task which checks the validity of the connection
     * every 30 seconds. The design of the feedback to the user has to be finetuned I
     * suppose, but I reckon this is better than nothing. Currently, only an error
     * dialog is displayed, and the icon of the tab is set to something different.
     * <p>
     * TODO: get rid of the magic numbers, they're arbitrarily chosen.
     */
    private void startConnectionValidityChecker() {
        connectionValidityTimer = new ConnectionValidityTimer(this, 10000);
        connectionValidityTimer.schedule();
    }

    /**
     * In case of a connection error, a throwable is set and the content of the tab
     * is changed with information as to why it failed.
     *
     * @param throwable The throwable.
     */
    public void setThrowable(final Throwable throwable) {
        connectionTimer.cancel();
        stackPane.getChildren().forEach(node -> {
            node.setVisible(false);
        });

        txtError.setVisible(true);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        String err =
                "Unable to connect\n\n" +
                        "Username: " + alias.getUser() + "\n" +
                        "URL:      " + alias.getUrl() + "\n" +
                        "Driver:   " + alias.getDriverClass() + "\n\n" +
                        "Stacktrace:\n\n" +
                        sw.toString();

        txtError.setText(err);
    }

    /**
     * Main entry point for creating the content of the tab.
     *
     * @return The node containing the contents.
     */
    private Node createContent() {
        progressIndicator = new ProgressIndicator();
        lblConnectionTime = new Label();

        // The content pane (which holds everything) is initially invisible when we're trying to connect.
        contentPane = new BorderPane();
        contentPane.setVisible(false);

        chkLimitRows = new CheckBox("Limit rows");
        chkLimitRows.setSelected(true);
        numMaxRowCount = new NumberTextField(100);
        HBox topPaneWithLabels = new HBox(chkLimitRows, numMaxRowCount);
        topPaneWithLabels.setPadding(new Insets(5));
        topPaneWithLabels.setAlignment(Pos.CENTER_LEFT);
        numMaxRowCount.disableProperty().bind(chkLimitRows.selectedProperty().not());

        contentPane.setTop(topPaneWithLabels);

        objectsTab = new ObjectsTab();
        sqlTab = new SQLTab(this);
        drillDownTab = new DrillDownTab(this);

        tabPane = new TabPane(objectsTab, sqlTab, drillDownTab);

        contentPane.setCenter(tabPane);

        // Create the textarea for errors.
        txtError = new TextArea();
        txtError.setEditable(false);
        txtError.getStyleClass().add("error-text");
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

    /**
     * Gets the maximum number of rows. If the value could not be parsed, return
     * the default 100.
     *
     * @return The maximum rows to return.
     */
    public int getLimitMaxRows() {
        if (!chkLimitRows.isSelected()) {
            return 0; // Anita Doth his ass!
        }

        Optional<Number> num = numMaxRowCount.getNumber();
        if (num.isPresent()) {
            return num.get().intValue();
        } else {
            logger.debug("Unable to properly parse '{}' as a number, using default 100", numMaxRowCount.getText());
            return 100;
        }
    }

    /**
     * Closes the selected result table, depending on which tab is selected.
     */
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
