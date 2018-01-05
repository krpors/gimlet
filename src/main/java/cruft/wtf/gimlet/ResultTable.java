package cruft.wtf.gimlet;


import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class represents a generic {@link TableView} for SQL queries. The table columns are therefore variadic depending
 * on the query executed. The table expects a {@link ResultSet} to iterate over. The class is not responsible for closing
 * the resources, merely displaying the data.
 */
public class ResultTable extends TableView {

    private static Logger logger = LoggerFactory.getLogger(ResultTable.class);

    private int rowCount = 0;

    public ResultTable() {
        setEditable(false);
        setTableMenuButtonVisible(true);
        setPlaceholder(new Label("Query is running..."));

        setRowFactory(param -> new ResultTableRow());
    }

    /**
     * Populates the table with the {@link ResultSet}. It is expected that this class is forwarding the cursor. Updating
     * the UI happens via the {@link Platform#runLater(Runnable)} utility function.
     *
     * @param rs The {@link ResultSet}.
     * @return The rowcount.
     * @throws SQLException When iterating the resultset fails for whatever reason.
     */
    @SuppressWarnings("unchecked")
    public int populate(final ResultSet rs) throws SQLException {
        rowCount = 0;

        Platform.runLater(() -> {
            getColumns().clear();
            getItems().clear();
        });

        ResultSetMetaData rsmd = rs.getMetaData();
        logger.debug("Resultset contains {} columns", rsmd.getColumnCount());


        TableColumn<ObservableList, Number> idCol = new TableColumn("#");
        idCol.setCellValueFactory(param -> new SimpleIntegerProperty((Integer) param.getValue().get(0)));
        Platform.runLater(() -> getColumns().add(idCol));

        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            final int j = i + 1;

            // This construction allows us to make the table sortable by numbers. Need to figure out though
            // if this needs a lot of expansion for all other types of integer-like column types.
            //
            // The reference to the actual data is a bit funky: it refers to the ObservableList containing the
            // row data (via setItems).
            if (rsmd.getColumnType(i + 1) == Types.INTEGER) {
                TableColumn<ObservableList, Number> column = new TableColumn<>(rsmd.getColumnName(i + 1));
                column.setCellValueFactory(param -> new SimpleIntegerProperty((Integer) param.getValue().get(j)));
                Platform.runLater(() -> getColumns().add(column));
            } else {
                TableColumn<ObservableList, String> col = new TableColumn<>(rsmd.getColumnName(i + 1));
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                Platform.runLater(() -> getColumns().add(col));
            }
        }



        ObservableList<ObservableList> rowdata = FXCollections.observableArrayList();
        while (rs.next()) {
            rowCount++;
            ObservableList columnContentLists = FXCollections.observableArrayList();
            columnContentLists.add(rowCount);
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                // first element is the actual row number, which is just the i counter;

                switch (rsmd.getColumnType(i)) {
                    case Types.BIGINT:
                        columnContentLists.add(rs.getLong(i));
                        break;
                    case Types.SMALLINT:
                    case Types.TINYINT:
                    case Types.INTEGER:
                        columnContentLists.add(rs.getInt(i));
                        break;
                    case Types.CHAR:
                    case Types.LONGNVARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.VARCHAR:
                        columnContentLists.add(rs.getString(i));
                        break;
                    case Types.TIMESTAMP:
                    case Types.TIMESTAMP_WITH_TIMEZONE:
                        columnContentLists.add(rs.getTimestamp(i));
                        break;
                    case Types.DATE:
                        columnContentLists.add(rs.getDate(i));
                        break;
                    case Types.TIME:
                        columnContentLists.add(rs.getTime(i));
                    default:
                        columnContentLists.add(rs.getString(i));
                }
            }

            rowdata.add(columnContentLists);
        }

        if (rowCount == 0) {
            Platform.runLater(() -> setPlaceholder(new Label("No results.")));
        }

        // Err... to prevent that the clearing of the items is done later than the setting, we also run the setting
        // of the items via Platform.runLater. This looks hacky as fuck but we'll manage for now.
        Platform.runLater(() -> {
            setItems(rowdata);
        });

        return rowCount;
    }

    private class Celelele extends TextFieldTableCell {
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setText("");
                return;
            }

            setText(item.toString());
        }
    }

    private class ResultTableRow extends TableRow<ObservableList> {
        @Override
        protected void updateItem(ObservableList rowData, boolean empty) {
            super.updateItem(rowData, empty);

            logger.debug("Upate of item {} (empty: {})", rowData, empty);
        }
    }
}
