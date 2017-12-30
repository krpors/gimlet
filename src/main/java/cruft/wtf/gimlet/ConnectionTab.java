package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ConnectionTab.class);

    private final Alias alias;

    private Connection connection;

    private ResultTable resultTable;

    private TextArea area;


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

    private void setQueryActive(boolean active) {
        area.setEditable(!active);

        if (active) {
            area.setEffect(new GaussianBlur(4));
        } else {
            area.setEffect(null);
        }
    }

    private Node createContent() {
        BorderPane pane = new BorderPane();

        FormPane fp = new FormPane();

        Label lbl = new Label();
        lbl.textProperty().bindBidirectional(alias.nameProperty());

        Label derp = new Label();
        derp.textProperty().bindBidirectional(alias.descriptionProperty());
        fp.add("Name", lbl);
        fp.add("Description", derp);

        pane.setTop(fp);

        TabPane tp = new TabPane();
        tp.getTabs().addAll(createTabQuery(), createTabDrillDown());

        pane.setCenter(tp);

        return pane;
    }

    private Tab createTabQuery() {
        Tab tabQuery = new Tab("Query");
        tabQuery.setClosable(false);
        tabQuery.setGraphic(Images.PULSE.imageView());

        BorderPane pane = new BorderPane();

        resultTable = new ResultTable();

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
                        resultTable.executeAndPopulate(connection, area.getText());
                        return null;
                    }
                };

                task.setOnRunning(event -> setQueryActive(true));
                task.setOnCancelled(event -> setQueryActive(false));
                task.setOnFailed(event -> setQueryActive(false));
                task.setOnSucceeded(event -> setQueryActive(false));

                Thread t = new Thread(task, "Gimlet Query Executor Thread");
                t.setDaemon(true);
                t.start();
            }
        });

        pane.setTop(area);
        pane.setCenter(resultTable);

        tabQuery.setContent(pane);

        return tabQuery;
    }

    private Tab createTabDrillDown() {
        Tab tab = new Tab("Drill down");
        tab.setGraphic(Images.COG.imageView());
        return tab;
    }
}
