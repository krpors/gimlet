package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        topPaneWithLabels.add("Name", lbl);
        topPaneWithLabels.add("Description", derp);

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

                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // TODO: cancellation on this task is not really possible.
                        //resultTable.executeAndPopulate(connection, area.getText());
                        ResultTable table = new ResultTable();
                        Tab tab = new Tab(area.getText());
                        tab.setContent(table);
                        Platform.runLater(() -> {
                            tabPaneResultSets.getTabs().add(tab);
                            tabPaneResultSets.getSelectionModel().select(tab);
                        });
                        table.executeAndPopulate(connection, area.getText());
                        return null;
                    }
                };

                Thread t = new Thread(task, "Gimlet Query Executor Thread");
                t.setDaemon(true);
                t.start();
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
        pane.setCenter(new Button("clix0r"));
        pane.setLeft(new Button("hello!"));

        tab.setContent(pane);

        return tab;
    }
}
