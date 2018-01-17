package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.ui.ConnectionTab;
import cruft.wtf.gimlet.ui.ConnectionTabPane;
import cruft.wtf.gimlet.ui.ResultTable;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

/**
 * Simple tester main to quickly test UI elements. Also: http://fxexperience.com/scenic-view/
 */
public class Tester extends Application {

    private void createWithResultTable(BorderPane pane) {
        ResultTable rs = new ResultTable();
        List<Column> cols = Arrays.asList(
                new Column(Types.CHAR, "FIRST_WITH_UNDERSCORE"),
                new Column(Types.CHAR, "SECOND"),
                new Column(Types.CHAR, "THIRD")
        );
        ObservableList<ObservableList> rowData = FXCollections.observableArrayList();
        rowData.addAll(
                FXCollections.observableArrayList(Arrays.asList("Kevin", "Pors", "Rulez. This contains a very long string blah laslkdjasldjalskdjal alsd jalks j")),
                FXCollections.observableArrayList(Arrays.asList(null, "Pors", "Rulez")),
                FXCollections.observableArrayList(Arrays.asList("Kevin", null, "Rulez")),
                FXCollections.observableArrayList(Arrays.asList("Kevin", "zzz", null))
        );
        rs.setColumns(cols);
        rs.setItems(rowData);

        pane.setCenter(rs);
    }

    private void createWithConnectionTabPane(BorderPane pane) throws SQLException {
        ConnectionTabPane ctp = new ConnectionTabPane();

        Alias alias = new Alias();
        alias.setUrl("jdbc:hsqldb:hsql:mem");
        alias.setColor("#8080ff");
        ConnectionTab tab = new ConnectionTab(alias);
        ctp.getTabs().add(tab);
        pane.setCenter(ctp);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane pane = new BorderPane();

        Scene scene = new Scene(pane);
        scene.getStylesheets().add("/css/style.css");

        primaryStage.setTitle("Tester");
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        createWithResultTable(pane);

        // Show the stage after possibly reading and setting window properties.
        primaryStage.show();
    }
}
