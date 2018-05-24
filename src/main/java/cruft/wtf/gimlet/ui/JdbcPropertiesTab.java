package cruft.wtf.gimlet.ui;

import javafx.collections.ObservableMap;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

import java.util.Map;

public class JdbcPropertiesTab extends Tab {

    private JdbcPropertiesTable table;


    public JdbcPropertiesTab() {
        super("JDBC properties");

        table = new JdbcPropertiesTable();

        Button btnAdd = new Button("", Images.PLUS.imageView());
        btnAdd.setOnAction(event -> {
            table.getItems().add(new JdbcPropertiesTable.JdbcProperty("property", "value"));
        });

        Button btnDel = new Button("", Images.TRASH.imageView());
        btnDel.setOnAction(event -> {
            table.getItems().remove(table.getSelectionModel().getSelectedItem());
        });

        ToolBar bar = new ToolBar(btnAdd, btnDel);

        BorderPane pane = new BorderPane();
        pane.setTop(bar);
        pane.setCenter(table);

        setContent(pane);
    }

    public ObservableMap<String, String> getItemsAsMap() {
        return table.getItemsAsMap();
    }

    public void setItemsFromMap(final Map<String, String> map) {
        table.setItemsFromMap(map);
    }
}
