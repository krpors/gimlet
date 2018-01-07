package cruft.wtf.gimlet;


import cruft.wtf.gimlet.conf.Query;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class is a specialization of the regular {@link ResultTable}. The main difference is that this class allows us
 * to 'drill down'.
 */
public class DrillResultTable extends ResultTable {

    private static Logger logger = LoggerFactory.getLogger(DrillResultTable.class);

    private final Query query;

    /**
     * The parent tab where this result table is added.
     */
    private ConnectionTab connectionTab;

    public DrillResultTable(final ConnectionTab connectionTab, final Query query) {
        this.connectionTab = connectionTab;
        this.query = query;
        setRowFactory(param -> new DrillResultTableRow());
    }


    /**
     * TableRow for this {@link DrillResultTable}. Contains context menus etc.
     */
    private class DrillResultTableRow extends TableRow<ObservableList> {

        private ContextMenu menu = new ContextMenu();

        public DrillResultTableRow() {
            for (Query subQuery : query.getSubQueries()) {
                MenuItem item = new MenuItem(subQuery.getName());
                menu.getItems().add(item);
                item.setOnAction(event -> {
                    executeDrillDown(subQuery);
                });
            }
        }

        public void executeDrillDown(final Query subquery) {
            logger.debug("Executing subquery {}", subquery.getName());

            ObservableList selectedItem = getSelectionModel().getSelectedItem();

            Map<String, Object> map = new TreeMap<>();
            for (int i = 0; i < getTableView().getColumns().size(); i++) {
                TableColumn thecol = getTableView().getColumns().get(i);
                String columnName = thecol.getText();
                map.put(columnName, selectedItem.get(i));
            }


            connectionTab.executeQuery(subquery, map);
        }

        @Override
        protected void updateItem(ObservableList item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                return;
            }

            Set<String> columns = new TreeSet<>();
            columns.addAll(Arrays.asList("lastname", "firstname"));

            Map<String, Integer> columnIndex = new HashMap<>();

            for (int i = 0; i < getTableView().getColumns().size(); i++) {
                TableColumn thecol = getTableView().getColumns().get(i);
                if (columns.contains(thecol.getText().toLowerCase())) {
                    columnIndex.put(thecol.getText().toLowerCase(), i);
                }
            }

//            menu.getItems().get(0).setOnAction(event -> {
//                ObservableList selectedItem = getTableView().getSelectionModel().getSelectedItem();
//                columnIndex.forEach((s, integer) -> logger.debug("Column {} is at {} => {}", s, integer, selectedItem.get(integer)));
//            });

            setContextMenu(menu);
        }


    }
}
