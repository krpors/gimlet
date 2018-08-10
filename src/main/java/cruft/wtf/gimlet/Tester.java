package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.jdbc.Column;
import cruft.wtf.gimlet.jdbc.ParseResult;
import cruft.wtf.gimlet.ui.*;
import cruft.wtf.gimlet.ui.controls.DateTimePicker;
import cruft.wtf.gimlet.ui.dialog.ParamInputDialog;
import cruft.wtf.gimlet.ui.dialog.QueryDialog;
import cruft.wtf.gimlet.ui.dialog.QueryReferenceDialog;
import cruft.wtf.gimlet.ui.dialog.SettingsDialog;
import cruft.wtf.gimlet.ui.objects.ObjectsTable;
import cruft.wtf.gimlet.ui.objects.ObjectsTableData;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

/**
 * Simple tester main to quickly test UI elements. Also: http://fxexperience.com/scenic-view/
 */
public class Tester extends Application {

    private void openQueryRefDialog() {
        List<Query> ql = new ArrayList<>();
        {
            Query q = new Query();
            q.setName("One");
            q.setDescription("Desc 1");
            ql.add(q);
            {
                Query q2 = new Query();
                q2.setName("One-One");
                q2.setName("Desc 1-1");
                q.getSubQueries().add(q2);
            }
        }
        {
            Query q = new Query();
            q.setName("Two");
            q.setDescription("Desc 2");
            ql.add(q);
            {
                Query q2 = new Query();
                q2.setName("Two-One");
                q2.setName("Desc 1-2");
                q.getSubQueries().add(q2);
            }
        }


        QueryReferenceDialog dlg = new QueryReferenceDialog(ql);
        Optional<Query> q = dlg.showAndWait();
        System.out.println(q);
    }

    private void openSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog();
        settingsDialog.showAndWait();
    }

    private void openQueryEditDialog() {
        QueryDialog dlg = new QueryDialog();
        dlg.showAndWait();
    }

    private void openParamInputDialog() {
        Set<ParseResult.Param> set = new TreeSet<>();
        set.add(new ParseResult.Param("ID", ParseResult.Type.NUMBER));
        set.add(new ParseResult.Param("NAME", ParseResult.Type.STRING));
        set.add(new ParseResult.Param("DATE", ParseResult.Type.DATE));
        set.add(new ParseResult.Param("DATETIME", ParseResult.Type.DATETIME));
        set.add(new ParseResult.Param("NUM", ParseResult.Type.NUMBER));
        set.add(new ParseResult.Param("NONE", ParseResult.Type.NONE));

        ParamInputDialog dlg = new ParamInputDialog(set);
        Map<String, Object> prefill = new HashMap<>();
        prefill.put("ID", 123);
        prefill.put("NAME", "Some name");
        prefill.put("DATE", new Date(System.currentTimeMillis()));
        prefill.put("DATETIME", new Timestamp(System.currentTimeMillis()));
        dlg.prefill(prefill);

        Optional<Map<String, Object>> map = dlg.showAndWait();
        if (map.isPresent()) {
            System.out.println("OK! " + map.get());
        } else {
            System.out.println("Naw.");
        }
    }

    private Tab createDialogTestTab() {
        Button btnOpenSettings = new Button("Settings");
        Button btnQueryEditDialog = new Button("Query editor");
        Button btnParamInputDialog = new Button("Param input");
        Button btnQueryRefDialog = new Button("Query ref");
        DateTimePicker dateTimePicker = new DateTimePicker();

        btnOpenSettings.setOnAction(event -> openSettingsDialog());
        btnQueryEditDialog.setOnAction(event -> openQueryEditDialog());
        btnParamInputDialog.setOnAction(event -> openParamInputDialog());
        btnQueryRefDialog.setOnAction(event -> openQueryRefDialog());

        FlowPane pane = new FlowPane(
                btnOpenSettings,
                btnQueryEditDialog,
                btnParamInputDialog,
                dateTimePicker,
                btnQueryRefDialog
        );
        pane.setHgap(5.0);
        pane.setPadding(new Insets(10));

        return new Tab("Buttons", pane);
    }

    private Tab createRotatedTable() {
        RotatedTable table = new RotatedTable();
        ObservableList<ObservableList> rowData = FXCollections.observableArrayList();
        rowData.addAll(
                FXCollections.observableArrayList("1", "Crufty"),
                FXCollections.observableArrayList("2", "Chainz"),
                FXCollections.observableArrayList("3", "Trippy")
        );
        table.setItems(Arrays.asList(new Column("Index"), new Column("Namesake")), rowData);

        return new Tab("Rotated table", table);
    }

    private Tab createWithObjectsTable() {
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

        return new Tab("Table", table);
    }

    private Tab createResultsTable() {
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
        rs.setItems(cols, rowData);

        return new Tab("Results table", rs);
    }

    private Tab createWithConnectionTabPane() throws SQLException {
        Alias alias = new Alias();
        alias.setName("Test");
        alias.setUser("SA");
        alias.setUrl("jdbc:hsqldb:hsql:mem");
        alias.setColor("#8080ff");
        ConnectionTab tab = new ConnectionTab(alias);
        ConnectionTabPane.instance.getTabs().add(tab);

        return new Tab("Connection tab", ConnectionTabPane.instance);
    }

    private Tab createWithJdbcProps() throws SQLException {
        JdbcPropertiesTab tab = new JdbcPropertiesTab();
        return tab;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TabPane pane = new TabPane();
        pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pane.getTabs().addAll(
                createWithJdbcProps(),
                createWithConnectionTabPane(),
                createWithObjectsTable(),
                createRotatedTable(),
                createResultsTable(),
                createDialogTestTab()
        );

        Scene scene = new Scene(pane);
        scene.getStylesheets().add("/css/style.css");

        primaryStage.setTitle("Tester");
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        // Show the stage after possibly reading and setting window properties.
        primaryStage.show();
    }
}
