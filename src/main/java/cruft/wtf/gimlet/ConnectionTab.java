package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.jdbc.NamedParameterPreparedStatement;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This tab is added to the parent tab pane when an SQL connection is established via de Alias thing on the left hand
 * side of the application.
 */
public class ConnectionTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ConnectionTab.class);

    private final Alias alias;

    private Connection connection;

    private TextArea area;

    private TabPane tabPaneResultSets;

    public ConnectionTab(final Alias alias) throws SQLException {
        this.alias = alias;

        setGraphic(Images.BOLT.imageView());
        textProperty().bindBidirectional(alias.nameProperty());

        connection = DriverManager.getConnection(alias.getUrl(), alias.getUser(), alias.getPassword());
        logger.info("Connection successfully established for alias '{}'", alias.getName());


        setOnCloseRequest(e -> {
            try {
                // Close the connection when the tab is closed.
                connection.close();
                logger.info("Closed connection for '{}'", alias.getName());
            } catch (SQLException e1) {
                logger.error("Could not close connection ourselves", e1);
            }
        });


        setContent(createContent());
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
        lbl.textProperty().bindBidirectional(alias.nameProperty());

        Label derp = new Label();
        derp.textProperty().bindBidirectional(alias.descriptionProperty());
        topPaneWithLabels.add("Name:", lbl);
        topPaneWithLabels.add("Description:", derp);

        pane.setTop(topPaneWithLabels);

        TabPane tp = new TabPane(createTabQuery(), createTabDrillDown());

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);

        splitPane.getItems().addAll(tp, createTabPaneResultTables());

        pane.setCenter(splitPane);

        return pane;
    }

    /**
     * Creates the tab pane containing the result tables.
     *
     * @return The TabPane.
     */
    private TabPane createTabPaneResultTables() {
        tabPaneResultSets = new TabPane();
        tabPaneResultSets.setTabMaxWidth(150.0);
        tabPaneResultSets.setSide(Side.BOTTOM);
        return tabPaneResultSets;
    }

    /**
     * Creates the static tab for testing out individual queries.
     *
     * @return The Tab.
     */
    private Tab createTabQuery() {
        Tab tabQuery = new Tab("Query");
        tabQuery.setClosable(false);
        tabQuery.setGraphic(Images.PULSE.imageView());

        BorderPane pane = new BorderPane();

        area = new TextArea();
        area.setText("select * from customer cross join invoice;");
        area.setWrapText(false);
        area.setPrefRowCount(5);
        area.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                try {
                    final PreparedStatement preparedStatement = connection.prepareStatement(area.getText());
                    executeQuery(preparedStatement, new ResultTable(), area.getText());
                } catch (SQLException ex) {
                    logger.error("Unable to prepare statement", ex);
                    Utils.showExceptionDialog("Unable to prepare statement", "See stacktrace below for more details", ex);
                }
            }
        });

        pane.setCenter(area);

        tabQuery.setContent(pane);

        return tabQuery;
    }

    private Tab createTabDrillDown() {
        Tab tab = new Tab("Drill down");
        tab.setClosable(false);
        tab.setGraphic(Images.COG.imageView());

        BorderPane pane = new BorderPane();

        ListView<Query> listView = new ListView<>();
        listView.itemsProperty().bindBidirectional(GimletApp.gimletProject.queriesProperty());
        listView.setCellFactory(param -> new QueryListViewListCell());
        listView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                System.out.println("Executing " + listView.getSelectionModel().getSelectedItem());
                executeQuery(listView.getSelectionModel().getSelectedItem());
            }
        });

        pane.setCenter(listView);

        tab.setContent(pane);

        return tab;
    }

    /**
     * Executes a predefined query.
     *
     * @param query The query shizzle.
     */
    private void executeQuery(final Query query) {
        assert query != null;

        try {
            NamedParameterPreparedStatement npsm =
                    NamedParameterPreparedStatement.createNamedParameterPreparedStatement(connection, query.getContent());
            if (npsm.hasNamedParameters()) {
                Set<String> params = npsm.getParameters();
                Map<String, String> map = new HashMap<>();
                params.forEach(s -> {
                    TextInputDialog tid = new TextInputDialog("");
                    tid.setTitle("Input");
                    tid.setHeaderText("Specify input for '" + s + "'");
                    Optional<String> opt = tid.showAndWait();
                    opt.ifPresent(s1 -> map.put(s, s1));
                    // TODO: on cancel... bail out.
                });


                for (String key : map.keySet()) {
                    try {
                        npsm.setString(key, map.get(key));
                    } catch (SQLDataException ex) {
                        logger.error("Invalid data given");
                        return;
                    } catch (SQLException e) {
                        logger.error("that didn't work...", e);
                        return;
                    }
                }
            }

            executeQuery(npsm, new DrillResultTable(this, query), query.getName());
        } catch (SQLException e) {
            logger.error("Could not prepare named parameter statement", e);
            Utils.showExceptionDialog("Bleh", "Yarp", e);
        }
    }

    /**
     * Executes a predefined query.
     *
     * @param query The query shizzle.
     */
    protected void executeQuery(final Query query, final Map<String, Object> columnValues) {
        assert query != null;

        try {
            NamedParameterPreparedStatement npsm =
                    NamedParameterPreparedStatement.createNamedParameterPreparedStatement(connection, query.getContent());
            for (String key : columnValues.keySet()) {
                npsm.setObject(key, columnValues.get(key));
            }

            executeQuery(npsm, new DrillResultTable(this, query), query.getName());
        } catch (SQLException e) {
            logger.error("Could not prepare named parameter statement", e);
            Utils.showExceptionDialog("Bleh", "Yarp", e);
        }
    }

    /**
     * Executes a query in String format. The resources will be closed by this method.
     *
     * @param stmt The statement to execute.
     */
    private void executeQuery(final PreparedStatement stmt, final ResultTable table, String tabText) {
        assert stmt != null;

        // A task is used, in another thread so the UI won't hang. All updates to the user interface are done
        // via Platform.runLater since JavaFX requires UI updates via the JavaFX application thread.

        TimedTask<Tab> task = new TimedTask<Tab>() {

            @Override
            protected Tab call() throws Exception {
                // TODO: cancellation on this task is not really possible.
                Tab tab = new Tab(tabText);
                tab.setContent(table);

                // Add the tab in the JavaFX App thread.
                Platform.runLater(() -> {
                    tabPaneResultSets.getTabs().add(tab);
                    tabPaneResultSets.getSelectionModel().select(tab);
                });

                // First, prepare the statement and execute it to see if we even can execute it.
                ResultSet rs = null;
                try {
                    start();
                    rs = stmt.executeQuery();
                    stop();

                    // Populate the table using the result set.
                    int rowcount = table.populate(rs);

                } catch (SQLException ex) {
                    // When exceptions occur, set the tab content to something else to say that something is
                    // screwed up. TODO: better reporting (text area?).
                    // Also, re-throw the exception to indicate the task has failed.
                    logger.error("Failed to execute query", ex);
                    Platform.runLater(() -> tab.setContent(new Label("Query failed: " + ex.getMessage())));
                    throw ex;
                } finally {
                    // Close the resources, if applicable.
                    stmt.close();

                    if (rs != null) {
                        rs.close();
                    }
                }

                return tab;
            }
        };

        task.timeProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Time taken:" + newValue);
        });

        // When the task failed (i.e. it threw an exception most likely) inform the user.
        task.setOnFailed(event -> {
            if (event.getSource().getException() != null) {
                Platform.runLater(() -> Utils.showExceptionDialog(
                        "Query failed.", "See stacktrace below for more details.", event.getSource().getException()));
            }
        });

        task.setOnSucceeded(event -> {
            Tab createdTab = (Tab) event.getSource().getValue();
            createdTab.setText(String.format("(%d ms.) %s", task.timeProperty().get(), createdTab.getText()));
        });

        // Create a thread, daemonize it and start it.
        Thread t = new Thread(task, "Gimlet Query Executor Thread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * A specialized list cell for a listview for the queries defined at the root of the app.
     */
    private class QueryListViewListCell extends TextFieldListCell<Query> {

        public QueryListViewListCell() {
        }

        @Override
        public void updateItem(Query item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                return;
            }

            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));
        }

    }
}
