package cruft.wtf.gimlet.ui.drilldown;

import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.QueryExecuteEvent;
import cruft.wtf.gimlet.ui.ConnectionTab;
import cruft.wtf.gimlet.ui.ConnectionTabPane;
import cruft.wtf.gimlet.ui.Images;
import cruft.wtf.gimlet.ui.ResultTableRow;
import cruft.wtf.gimlet.ui.controls.LabeledSeparatorMenuItem;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TableRow for this {@link DrillResultTable}, and is a specialization on the basic {@link ResultTableRow}.
 * This table row contains extra items in the context menu for the drilldown functionality.
 */
class DrillResultTableRow extends ResultTableRow {

    private static Logger logger = LoggerFactory.getLogger(DrillResultTableRow.class);

    private final Query query;

    public DrillResultTableRow(final Query query) {
        this.query = query;

        if (!query.getSubQueries().isEmpty()) {
            contextMenu.getItems().add(new SeparatorMenuItem());
        }

        createMenuItems();

        // TODO: the ListChangeListener produces a buttload of events when a list changed.
        // It doesn't have an impact on performance ... yet but it's not optimal either.

        // Note: the listener will be added per visible table row.
        query.subQueriesProperty().addListener((ListChangeListener<Query>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasReplaced() || c.wasRemoved()) {
                    createMenuItems();
                }
            }
        });

        query.referencedQueriesProperty().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasReplaced() || c.wasRemoved()) {
                    createMenuItems();
                }
            }
        });

        ConnectionTabPane.instance.getTabs().addListener((ListChangeListener<? super Tab>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasReplaced() || c.wasRemoved()) {
                    createMenuItems();
                }
            }
        });
    }

    /**
     * Adds subquery menu items for this table row.
     */
    private void createMenuItems() {
        // First remove all items. They can be added again when the list of subqueries change.
        contextMenu.getItems().clear();
        // re-add the menu for copying table data.
        // TODO: find an more neat way to re-add the menuCopy instead of using a protected var.
        contextMenu.getItems().add(menuCopy);

        createMenuItemsSubQueries(contextMenu.getItems(), null);
        createMenuItemsRefQueries(contextMenu.getItems(), null);

        // If the ConnectionTabPane has more than 1 connection open, we
        // allow the user to invoke subqueries on other connections. This
        // grants the user the possibility to drill down into a different
        // datasource with a different user. This is especially handy when
        // certain data resides somewhere else, but cannot be queried via
        // the original datasource (because it's on a different host, server,
        // schema, etc.).
        if (ConnectionTabPane.instance.getTabs().size() > 1) {
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(new LabeledSeparatorMenuItem("Interconnection"));

            for (Tab tab : ConnectionTabPane.instance.getTabs()) {
                // Don't give an extra interconnection entry for the current
                // selected tab. That would be rather stupid and redundant.
                if (tab.isSelected()) {
                    continue;
                }

                ConnectionTab ct = (ConnectionTab) tab;

                Menu menuAlias = new Menu(ct.getAlias().getName(), Images.ACCOUNT_LOGIN.imageView());
                createMenuItemsSubQueries(menuAlias.getItems(), ct.getText());
                createMenuItemsRefQueries(menuAlias.getItems(), ct.getText());

                contextMenu.getItems().add(menuAlias);

            }
        }
    }

    private void createMenuItemsSubQueries(ObservableList<MenuItem> listToAddto, String targetTab) {
        // Create a context menu, containing the direct sub queries for the original query.
        if (!query.getSubQueries().isEmpty()) {
            listToAddto.add(new SeparatorMenuItem());
            listToAddto.add(new LabeledSeparatorMenuItem("Subqueries"));
        }

        for (Query subQuery : query.getSubQueries()) {
            MenuItem item = new MenuItem(subQuery.getName());
            item.setMnemonicParsing(false);
            listToAddto.add(item);
            item.setOnAction(event -> executeDrillDown(subQuery, targetTab));
        }
    }

    private void createMenuItemsRefQueries(ObservableList<MenuItem> listToAddTo, String targetTab) {
        // Check if the query has configured referenced queries (reusing of other, existing queries).
        // If so, add them as well.
        List<Query> refQueries = query.findReferencesQueries();
        if (!refQueries.isEmpty()) {
            listToAddTo.add(new SeparatorMenuItem());
            listToAddTo.add(new LabeledSeparatorMenuItem("References"));
        }

        for (Query ref : refQueries) {
            MenuItem item = new MenuItem(ref.getName());
            listToAddTo.add(item);
            item.setOnAction(event -> executeDrillDown(ref, targetTab));
        }
    }

    private void executeDrillDown(final Query subquery, final String targetTab) {
        logger.debug("Executing subquery '{}'", subquery.getName());

        ObservableList selectedItem = getTableView().getSelectionModel().getSelectedItem();

        // Get all columns of a row, and put the values in a map where the key is the column name,
        // and the value is the actual value of that column in the selected row.
        Map<String, Object> map = new TreeMap<>();
        for (int i = 0; i < getTableView().getColumns().size(); i++) {
            TableColumn thecol = getTableView().getColumns().get(i);
            String columnName = thecol.getText();
            map.put(columnName, selectedItem.get(i));
        }

        // Create an event, post it on the event bus to notify listeners that we're going to execute a
        // drilldown query.
        QueryExecuteEvent executeEvent = new QueryExecuteEvent();
        executeEvent.setTargetTab(targetTab);
        executeEvent.setQuery(subquery);
        executeEvent.setColumnnMap(map);
        EventDispatcher.getInstance().post(executeEvent);
    }

    @Override
    protected void updateItem(ObservableList item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            return;
        }

        setContextMenu(contextMenu);
    }
}
