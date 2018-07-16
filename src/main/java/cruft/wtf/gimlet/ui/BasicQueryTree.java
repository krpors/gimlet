package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.conf.Query;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;

import java.util.List;
import java.util.Optional;

/**
 * This is a way more basic version of the original {@link QueryTree}. It differs a lot in that it's
 * just a tree used for selecting a reference/linked query. Therefore it does not need to comprehensiveness
 * of menu's, listeners, drag-and-drop logic etc.
 * <p>
 * I did not feel like generifying the {@link QueryTree} so it contains both behaviours so I just copied
 * and pasted some lines here and there. So fucking shoot me, I think this is more readable and easier to
 * reason about!
 */
public class BasicQueryTree extends TreeView<Query> {

    public BasicQueryTree() {
    }

    /**
     * Sets the list of queries to display in the tree.
     *
     * @param queryList The list of queries.
     */
    public void setQueryList(final List<Query> queryList) {
        TreeItem<Query> root = new TreeItem<>();

        addQuery(root, queryList);

        setShowRoot(false);
        setCellFactory(param -> new QueryConfigurationTreeCell());

        setRoot(root);
    }

    /**
     * Adds queries recursively to the tree from the given queryList.
     *
     * @param root      The root to place the queryList under.
     * @param queryList The queryList source.
     */
    private void addQuery(final TreeItem<Query> root, List<Query> queryList) {
        if (queryList == null || queryList.size() == 0) {
            return;
        }

        for (Query q : queryList) {
            TreeItem<Query> qitem = new TreeItem<>(q);
            root.getChildren().add(qitem);
            addQuery(qitem, q.getSubQueries());
            // Note: this class does not add the referenced queries since that would be useless
            // to do anyway.
        }

        root.setExpanded(true);
    }

    /**
     * Gets the selected item, or an empty optional if nothing has been selected.
     *
     * @return The (optional) query which was selected from the tree.
     */
    public Optional<Query> getSelected() {
        if (getSelectionModel().getSelectedItem() != null) {
            return Optional.of(getSelectionModel().getSelectedItem().getValue());
        }
        return Optional.empty();
    }


    /**
     * Contains rendering logic for {@link Query} objects used throughout Gimlet.
     */
    private class QueryConfigurationTreeCell extends TextFieldTreeCell<Query> {

        @Override
        public void updateItem(Query item, boolean empty) {
            // super call is required, see documentation.
            super.updateItem(item, empty);

            if (empty || item == null) {
                getStyleClass().remove("query-reference");
                setText(null);
                setGraphic(null);
                setTooltip(null);
                setContextMenu(null); // set to null so the 'add root query' context menu can be made visible.
                return;
            }

            setText(item.getName());


            // If the given query item is marked as a reference, we are not allowed to do anything
            // on it. So disable the normal context menu, but mark it as a reference. Prematurely
            // return, since we are not allowed to do drag-and-drop actions etc. on that.
            if (item.isReference()) {
                return;
            }

            if (item.getParentQuery() == null) {
                setStyle("-fx-base: #c0c0c0");
                setGraphic(Images.MAGNIFYING_GLASS.imageView());
            }
            setTooltip(new Tooltip(item.getDescription()));
        }
    }
}
