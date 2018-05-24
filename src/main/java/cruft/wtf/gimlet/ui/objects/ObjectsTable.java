package cruft.wtf.gimlet.ui.objects;

import cruft.wtf.gimlet.ui.drilldown.DrillResultTable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

public class ObjectsTable extends TableView<ObjectsTableData> {

    public ObjectsTable() {
        setEditable(false);
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);

        setRowFactory(param -> new ObjectsTableRowFactory());

        TableColumn<ObjectsTableData, String> columnColName = new TableColumn<>("Column name");
        columnColName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getColumnName()));
        columnColName.setMinWidth(120);
        getColumns().add(columnColName);

        TableColumn<ObjectsTableData, String> columnColDataType = new TableColumn<>("Data type");
        columnColDataType.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDataType()));
        getColumns().add(columnColDataType);

        TableColumn<ObjectsTableData, String> columnColTypeName = new TableColumn<>("Type name");
        columnColTypeName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTypeName()));
        getColumns().add(columnColTypeName);

        TableColumn<ObjectsTableData, Number> columnColSize = new TableColumn<>("Size");
        columnColSize.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getColumnSize()));
        getColumns().add(columnColSize);

        TableColumn<ObjectsTableData, Boolean> columnColNullable = new TableColumn<>("Nullable");
        columnColNullable.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isNullable()));
        getColumns().add(columnColNullable);

        TableColumn<ObjectsTableData, Boolean> columnColPrimaryKey = new TableColumn<>("Primary key");
        columnColPrimaryKey.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isPrimaryKey()));
        columnColPrimaryKey.setMinWidth(120);
        getColumns().add(columnColPrimaryKey);

        TableColumn<ObjectsTableData, Number> columnColOrdinalPos = new TableColumn<>("Ordinal");
        columnColOrdinalPos.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getOrdinalPosition()));
        getColumns().add(columnColOrdinalPos);

        TableColumn<ObjectsTableData, String> columnRemarks = new TableColumn<>("Remarks");
        columnRemarks.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRemarks()));
        getColumns().add(columnRemarks);
    }

    /**
     * TableRow for this {@link DrillResultTable}. Contains context menus etc.
     */
    private class ObjectsTableRowFactory extends TableRow<ObjectsTableData> {

        public ObjectsTableRowFactory() {
        }

        @Override
        protected void updateItem(ObjectsTableData item, boolean empty) {
            super.updateItem(item, empty);

            getStyleClass().remove("primary-key");

            if (item == null || empty) {
                return;
            }


            if (item.isPrimaryKey()) {
                getStyleClass().add("primary-key");
            }
        }
    }
}
