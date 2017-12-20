package cruft.wtf.gimlet;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.QuerySavedEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.KeyCode;

import java.util.List;

public class QueryTree extends TreeView<Query> {

    public QueryTree() {
        EventDispatcher.getInstance().register(this);
    }

    public void setQueryConfiguration(final List<Query> queryList) {
        TreeItem<Query> root = new TreeItem<>();

        addQuery(root, queryList);

        setShowRoot(false);
        setCellFactory(param -> new QueryConfigurationTreeCell());
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.println("Selected " + getSelectionModel().getSelectedItem().getValue().getName());
            }
        });
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                TreeItem<Query> selectedItem = getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    return;
                }

                QueryEditDialog qed = new QueryEditDialog(selectedItem.getValue());
                qed.showAndWait();
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

        root.setExpanded(true);
    }

    @Subscribe
    public void onQuerySaved(QuerySavedEvent event) {
        refresh();
    }

    /**
     * Contains rendering logic for {@link Query} objects used throughout Gimlet.
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
