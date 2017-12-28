package cruft.wtf.gimlet;


import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class ResultTable extends TableView {

    public ResultTable() {
        setEditable(false);
    }

    public void populate(final ResultSet rs) throws SQLException {
        getColumns().clear();
        getItems().clear();

        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            final int j = i;

            // This construction allows us to make the table sortable by numbers. Need to figure out though
            // if this needs a lot of expansion for all other types of integer-like column types.
            if (rsmd.getColumnType(i + 1) == Types.INTEGER) {
                TableColumn<ObservableList, Number> column = new TableColumn<>(rsmd.getColumnName(i + 1));
                column.setCellValueFactory(param -> new SimpleIntegerProperty((Integer) param.getValue().get(j)));
                getColumns().add(column);
            } else {
                TableColumn<ObservableList, String> col = new TableColumn<>(rsmd.getColumnName(i + 1));
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                getColumns().add(col);
            }
            System.out.println("Added column " + i);
        }

        ObservableList<ObservableList> rowdata = FXCollections.observableArrayList();
        while (rs.next()) {
            ObservableList werd = FXCollections.observableArrayList();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                if (rsmd.getColumnType(i) == Types.INTEGER) {
                    System.out.println("integer!");
                    werd.add(rs.getInt(i));
                } else {
                    werd.add(rs.getString(i));
                }
            }

            rowdata.add(werd);
        }

        setItems(rowdata);
    }
}
