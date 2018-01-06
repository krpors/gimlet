package cruft.wtf.gimlet;


import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;

/**
 * This class represents a generic {@link TableView} for SQL queries. The table columns are therefore variadic depending
 * on the query executed. The table expects a {@link ResultSet} to iterate over. The class is not responsible for closing
 * the resources, merely displaying the data.
 */
public class DrillResultTable extends ResultTable {

    private static Logger logger = LoggerFactory.getLogger(DrillResultTable.class);

    public DrillResultTable() {
        setRowFactory(param -> new Rowow());
    }


    private class Rowow extends TableRow<ObservableList> {

        private ContextMenu menu = new ContextMenu();

        public Rowow() {
            MenuItem item = new MenuItem("HERRO");
            menu.getItems().add(item);
        }

        @Override
        protected void updateItem(ObservableList item, boolean empty) {
            super.updateItem(item, empty);

            Set<String> columns = new TreeSet<>();
            columns.addAll(Arrays.asList("lastname", "firstname"));

            Map<String, Integer> columnIndex = new HashMap<>();

            for (int i = 0; i < getTableView().getColumns().size(); i++) {
                TableColumn thecol = getTableView().getColumns().get(i);
                if (columns.contains(thecol.getText().toLowerCase())) {
                    columnIndex.put(thecol.getText().toLowerCase(), i);
                }
            }

            menu.getItems().get(0).setOnAction(event -> {
                ObservableList selectedItem = getTableView().getSelectionModel().getSelectedItem();
                columnIndex.forEach((s, integer) -> logger.debug("Column {} is at {} => {}", s, integer, selectedItem.get(integer)));
            });

            setContextMenu(menu);
        }


    }
}
