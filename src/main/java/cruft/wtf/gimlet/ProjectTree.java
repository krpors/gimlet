package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Item;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.conf.QueryConfiguration;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ProjectTree extends TreeView<Item> {

    public ProjectTree() {
    }

    public void setQueryConfiguration(final QueryConfiguration configuration) {
        TreeItem<Item> root = new TreeItem<>(configuration);

        addQuery(root, configuration.getQueries());

        super.setCellFactory(new Callback<TreeView<Item>, TreeCell<Item>>() {
            @Override
            public TreeCell<Item> call(TreeView<Item> param) {
                ProjectTreeCell projectTreeCell = new ProjectTreeCell();
                return projectTreeCell;
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

    private class ProjectTreeCell extends TreeCell<Item> {

        public ProjectTreeCell() {
            setText("www");


        }

        @Override
        protected void updateItem(Item item, boolean empty) {
            // super call is required, see documentation.
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }

            if (item instanceof QueryConfiguration) {
                setText("CONFIG: " + item.getName());
            } else {
                setText(item.getName());
                setTooltip(new Tooltip(item.getDescription()));
            }
        }
    }
}
