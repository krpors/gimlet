package cruft.wtf.gimlet.ui;


import cruft.wtf.gimlet.Column;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The {@link RotatedTable} is table which rotates the result, so that each column is a row number, and each row
 * is a column. So instead of:
 * <table>
 * <tr>
 * <th>ID</th>
 * <th>Name</th>
 * <th>Surname</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>Roger</td>
 * <td>Wilco</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>Gordon</td>
 * <td>Freeman</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>Lara</td>
 * <td>Croft</td>
 * </tr>
 * </table>
 * The table will be displayed as:
 * <table>
 * <tr>
 * <th>Column name</th>
 * <th>Row 0</th>
 * <th>Row 1</th>
 * <th>Row 2</th>
 * </tr>
 * <tr>
 * <td>ID</td>
 * <td>1</td>
 * <td>2</td>
 * <td>3</td>
 * </tr>
 * <tr>
 * <td>Name</td>
 * <td>Roger</td>
 * <td>Gordon</td>
 * <td>Lara</td>
 * </tr>
 * <tr>
 * <td>Wilco</td>
 * <td>Freeman</td>
 * <td>Croft</td>
 * </tr>
 * </table>
 */
public class RotatedTable extends ResultTable {

    private static Logger logger = LoggerFactory.getLogger(RotatedTable.class);

    private boolean loaded = false;

    public RotatedTable() {
        setPlaceholder(new Label("Rotating original results..."));
        setTableMenuButtonVisible(false);
    }

    /**
     * Sets the items. This method will 'rotate' the items based on the parameters.
     *
     * @param columnList The column list.
     * @param list       The initial, non-rotated table data.
     */
    @Override
    public void setItems(List<Column> columnList, ObservableList<ObservableList> list) {
        if (!loaded) {
            loaded = true;
        } else {
            return;
        }

        if (list.size() <= 0) {
            setPlaceHolderNoResults();
            return;
        }

        // Start by adding the columns. They're known before hand.
        TableColumn<ObservableList, Object> sup = new TableColumn<>("Column name");
        sup.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().get(0)));
        sup.setCellFactory(param -> new ResultSetCell());
        getColumns().add(sup);

        for (int row = 0; row < list.size(); row++) {
            final int i = row;
            TableColumn<ObservableList, Object> yo = new TableColumn<>("Row " + row);
            yo.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().get(i + 1)));
            getColumns().add(yo);
        }

        ObservableList<ObservableList> rotated = FXCollections.observableArrayList();
        // First iterate over all the known columns.
        for (int i = 0; i < columnList.size(); i++) {
            // The first entry in the rotated data is the column name in the list of columns.
            ObservableList<Object> rotatedRowData = FXCollections.observableArrayList();
            rotatedRowData.add(columnList.get(i).getColumnName());
            // Then iterate over the actual data.
            for (ObservableList yo : list) {
                // Get the entry, but get the data at the current column index (i) position.
                rotatedRowData.add(yo.get(i));
            }
            // Add the rotated data.
            rotated.add(rotatedRowData);
        }

        // Last but not least, add the items to the TableView.
        setItems(rotated);
    }
}
