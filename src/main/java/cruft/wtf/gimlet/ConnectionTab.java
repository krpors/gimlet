package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.jdbc.NamedParameterPreparedStatement;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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

    private TabPane createTabPaneResultTables() {
        tabPaneResultSets = new TabPane();
        tabPaneResultSets.setTabMaxWidth(150.0);
        tabPaneResultSets.setSide(Side.BOTTOM);
        return tabPaneResultSets;
    }

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
                executeQuery(area.getText());
            }
        });

        pane.setCenter(area);

        tabQuery.setContent(pane);

        return tabQuery;
    }

    private Tab createTabDrillDown() {
        Tab tab = new Tab("Drill down");
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
                    tid.showAndWait().ifPresent(s1 -> map.put(s, s1));
                });

                map.forEach((s, s2) -> {
                    try {
                        npsm.setString(s, s2);
                    } catch (SQLException e) {
                        logger.error("that didn't work...", e);
                    }
                });


                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // TODO: cancellation on this task is not really possible.
                        ResultTable table = new ResultTable();
                        Tab tab = new Tab(query.getName());
                        tab.setContent(table);
                        Platform.runLater(() -> {
                            tabPaneResultSets.getTabs().add(tab);
                            tabPaneResultSets.getSelectionModel().select(tab);
                        });
                        table.executeAndPopulate(npsm);
                        return null;
                    }
                };

                Thread t = new Thread(task, "Gimlet Query Executor Thread");
                t.setDaemon(true);
                t.start();
            }

        } catch (SQLException e) {
            logger.error("Could not prepare named parameter statement", e);
            Utils.showExceptionDialog("Bleh", "Yarp", e);
        }

    }

    /**
     * Executes a query in String format.
     *
     * @param query The query to execute.
     */
    private void executeQuery(final String query) {
        assert query != null;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // TODO: cancellation on this task is not really possible.
                ResultTable table = new ResultTable();
                Tab tab = new Tab(query);
                tab.setContent(table);
                Platform.runLater(() -> {
                    tabPaneResultSets.getTabs().add(tab);
                    tabPaneResultSets.getSelectionModel().select(tab);
                });
                table.executeAndPopulate(connection, query);
                return null;
            }
        };

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
