package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Item;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.conf.QueryConfiguration;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

import java.util.List;

public class QueryConfigurationTree extends TreeView<Item> {

    public QueryConfigurationTree() {
    }

    public void setQueryConfiguration(final QueryConfiguration configuration) {
        TreeItem<Item> root = new TreeItem<>(configuration);

        addQuery(root, configuration.getQueries());

        setEditable(true);
        super.setCellFactory(param -> new QueryConfigurationTreeCell());

        setRoot(root);
    }

    /**
     * Adds queries recursively.
     *
     * @param root
     * @param queryList
     */
    private void addQuery(final TreeItem<Item> root, List<Query> queryList) {
        if (queryList == null || queryList.size() == 0) {
            return;
        }

        for (Query q : queryList) {
            TreeItem<Item> qitem = new TreeItem<>(q);
            root.getChildren().add(qitem);
            addQuery(qitem, q.getSubQueries());
        }
    }

    /**
     * Contains rendering logic for {@link Item} objects used throughout Gimlet.
     */
    private class QueryConfigurationTreeCell extends TextFieldTreeCell<Item> {

        private ContextMenu menu = new ContextMenu();

        public QueryConfigurationTreeCell() {
            super(new ItemConverter());
            // TODO: set item converter.
            MenuItem renameItem = new MenuItem("Rename");
            menu.getItems().add(renameItem);
            renameItem.setOnAction(e -> {
                startEdit();
            });
        }

        @Override
        public void updateItem(Item item, boolean empty) {
            // super call is required, see documentation.
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }

            if (!isEditing()) {
                this.setContextMenu(menu);
            }

            if (item instanceof QueryConfiguration) {
                setText("CONFIG: " + item.getName());
            } else {
                setText(item.getName());
                setTooltip(new Tooltip(item.getDescription()));
            }
        }

    }

    private class ItemConverter extends StringConverter<Item> {

        @Override
        public String toString(Item object) {
            return object.getName();
        }

        @Override
        public Item fromString(String string) {
            return new Query();
        }
    }
}
