package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Item;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.conf.QueryConfiguration;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

import java.util.List;

public class QueryConfigurationTree extends TreeView<Query> {

    public QueryConfigurationTree() {
    }

    public void setQueryConfiguration(final QueryConfiguration configuration) {
        TreeItem<Query> root = new TreeItem<>();

        addQuery(root, configuration.getQueries());

        setShowRoot(false);
        setCellFactory(param -> new QueryConfigurationTreeCell());
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.println("Selected " + getSelectionModel().getSelectedItem().getValue().getName());
            }
        });
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                System.out.println("Entered " + getSelectionModel().getSelectedItem().getValue().getName());
            }
        });

        setRoot(root);
    }

    /**
     * Adds queries recursively.
     *
     * @param root
     * @param queryList
     */
    private void addQuery(final TreeItem<Query> root, List<Query> queryList) {
        if (queryList == null || queryList.size() == 0) {
            return;
        }

        for (Query q : queryList) {
            TreeItem<Query> qitem = new TreeItem<>(q);
            root.getChildren().add(qitem);
            addQuery(qitem, q.getSubQueries());
        }
    }

    /**
     * Contains rendering logic for {@link Item} objects used throughout Gimlet.
     */
    private class QueryConfigurationTreeCell extends TextFieldTreeCell<Query> {

        private ContextMenu menu = new ContextMenu();

        public QueryConfigurationTreeCell() {
//            super(new ItemConverter());
            // TODO: set item converter.
            MenuItem renameItem = new MenuItem("Rename");
            menu.getItems().add(renameItem);
            renameItem.setOnAction(e -> {
                startEdit();
            });
        }

        @Override
        public void updateItem(Query item, boolean empty) {
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

            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));

        }
    }
}
