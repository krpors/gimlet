package cruft.wtf.gimlet.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.util.Map;
import java.util.TreeMap;

/**
 * This table contains functionality for custom JDBC driver properties.
 */
public class JdbcPropertiesTable extends TableView<JdbcPropertiesTable.JdbcProperty> {

    public JdbcPropertiesTable() {
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
        setPrefHeight(120);

        TableColumn<JdbcProperty, String> col1 = new TableColumn<>("Name");
        col1.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        col1.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        col1.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue()));
        getColumns().add(col1);

        TableColumn<JdbcProperty, String> col2 = new TableColumn<>("Value");
        col2.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        col2.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue()));
        col2.setOnEditCommit(event -> event.getRowValue().setValue(event.getNewValue()));
        getColumns().add(col2);
    }

    public ObservableMap<String, String> getItemsAsMap() {
        ObservableMap<String, String> m = FXCollections.observableMap(new TreeMap<>());
        getItems().forEach(jdbcProperty -> m.put(jdbcProperty.getName(), jdbcProperty.getValue()));
        return m;
    }

    public void setItemsFromMap(final Map<String, String> map) {
        ObservableList<JdbcPropertiesTable.JdbcProperty> props = FXCollections.observableArrayList();
        map.forEach((k, v) -> props.add(new JdbcPropertiesTable.JdbcProperty(k, v)));
        setItems(props);
    }

    /**
     * Denotes a name/value for the table.
     */
    public static class JdbcProperty {
        private String name;
        private String value;

        public JdbcProperty() {
        }

        public JdbcProperty(String name, String value) {
            setName(name);
            setValue(value);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
