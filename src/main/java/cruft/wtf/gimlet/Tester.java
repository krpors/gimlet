package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.jdbc.ParseResult;
import cruft.wtf.gimlet.ui.*;
import cruft.wtf.gimlet.ui.dialog.ParamInputDialog;
import cruft.wtf.gimlet.ui.dialog.QueryDialog;
import cruft.wtf.gimlet.ui.dialog.SettingsDialog;
import cruft.wtf.gimlet.ui.objects.ObjectsTable;
import cruft.wtf.gimlet.ui.objects.ObjectsTableData;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Simple tester main to quickly test UI elements. Also: http://fxexperience.com/scenic-view/
 */
public class Tester extends Application {

    private void withSettingsDialog(BorderPane pane) {
        SettingsDialog settingsDialog = new SettingsDialog();
        settingsDialog.showAndWait();
    }

    private void withQueryEditDialog(BorderPane pane) {
        QueryDialog dlg = new QueryDialog();
        dlg.showAndWait();
    }

    private void withParamInputDialog(BorderPane pane) {
        Set<ParseResult.Param> set = new TreeSet<>();
        set.add(new ParseResult.Param("ID", ParseResult.Type.NUMBER));
        set.add(new ParseResult.Param("NAME", ParseResult.Type.STRING));
        set.add(new ParseResult.Param("DATE", ParseResult.Type.DATE));
        set.add(new ParseResult.Param("NUM", ParseResult.Type.NUMBER));
        set.add(new ParseResult.Param("NONE", ParseResult.Type.NONE));

        ParamInputDialog dlg = new ParamInputDialog(set);
        Optional<Map<String,Object>> map = dlg.showAndWait();
        if (map.isPresent()) {
            System.out.println("OK! " + map.get());
        } else {
            System.out.println("Naw.");
        }
    }

    private void createWithObjectsTable(BorderPane pane) {
        ObjectsTable table = new ObjectsTable();

        ObservableList<ObjectsTableData> a = FXCollections.observableArrayList();
        {
            ObjectsTableData d = new ObjectsTableData();
            d.setColumnName("SOME_COL");
            d.setPrimaryKey(true);
            a.add(d);
        }
        {
            ObjectsTableData d = new ObjectsTableData();
            d.setColumnName("NOPRIM");
            d.setPrimaryKey(false);
            a.add(d);
        }

        table.setItems(a);

        pane.setCenter(table);
    }

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

        withParamInputDialog(pane);

        // Show the stage after possibly reading and setting window properties.
        primaryStage.show();
    }
}
