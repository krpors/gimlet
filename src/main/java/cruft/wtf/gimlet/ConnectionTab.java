package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class ConnectionTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ConnectionTab.class);

    private final Alias alias;

    private Connection connection;

    private ResultTable resultTable;


    public ConnectionTab(final Alias alias) throws SQLException {
        this.alias = alias;

        setGraphic(Images.BOLT.imageView());
        textProperty().bindBidirectional(alias.nameProperty());

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


        setContent(createContent());
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
        Tab tabQuery = new Tab("Query", new Button("Kevin"));
        tabQuery.setClosable(false);
        tabQuery.setGraphic(Images.PULSE.imageView());

        BorderPane pane = new BorderPane();

        resultTable = new ResultTable();

        TextArea area = new TextArea();
        area.setWrapText(false);
        area.setPrefRowCount(5);
        area.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                try (PreparedStatement stmt = connection.prepareStatement(area.getText());
                     ResultSet rs = stmt.executeQuery()) {
                    resultTable.populate(rs);
                } catch (SQLException ex) {
                    logger.error("Could not execute query", ex);
                    Utils.showExceptionDialog("Could not execute query", "Bleh", ex);
                }
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
