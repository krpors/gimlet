package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.QueryExecuteEvent;
import cruft.wtf.gimlet.jdbc.NamedParameterPreparedStatement;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QueryTree extends TreeView<Query> {

    private static Logger logger = LoggerFactory.getLogger(QueryTree.class);

    /**
     * The original assigned query list.
     */
    private List<Query> queryList;

    private ObjectProperty<Query> copiedQueryProperty = new SimpleObjectProperty<>();

    private TreeItem<Query> sourceDraggedItem;

    public QueryTree() {
        EventDispatcher.getInstance().register(this);
    }

    /**
     * Sets the list of queries to display in the tree.
     *
     * @param queryList The list of queries.
     */
    public void setQueryList(final List<Query> queryList) {
        this.queryList = queryList;

        TreeItem<Query> root = new TreeItem<>();

        addQuery(root, queryList);

        setShowRoot(false);
        setCellFactory(param -> new QueryConfigurationTreeCell());

        setRoot(root);

        ContextMenu menu = new ContextMenu();
        MenuItem newRootQuery = new MenuItem("New root query", Images.PLUS.imageView());
        newRootQuery.setOnAction(event -> {
            QueryEditDialog qed = new QueryEditDialog();
            qed.showAndWait();
            if (qed.getResult() == ButtonType.OK) {
                Query query = qed.getQuery();
                // Add a TreeItem to the root, and to the root of the querylist.
                getRoot().getChildren().add(new TreeItem<>(query));
                queryList.add(query);
            }
        });
        menu.getItems().add(newRootQuery);
        setContextMenu(menu);
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
        }

        root.setExpanded(true);
    }

    /**
     * Asks the user via text input dialogs for values.
     *
     * @param query The Query to parse.
     * @return An optional map of string mapped to objects. If an empty Optional is returned, the user has any of the input dialogs.
     */
    private Optional<Map<String, Object>> askInputForQuery(final Query query) {
        NamedParameterPreparedStatement.ParseResult result = NamedParameterPreparedStatement.parse(query.getContent());
        if (result.getUniqueParameters().isEmpty()) {
            return Optional.of(Collections.emptyMap());
        }

        ParamInputDialog dlg = new ParamInputDialog(result.getUniqueParameters());
        return dlg.showAndWait();
    }

    /**
     * Exectues the {@link Query} which has been selected to run in this tree.
     *
     * @param query The query to run.
     */
    private void executeSelectedQuery(final Query query) {
        // If the optional is not present (empty), user has cancelled the input dialog.
        // If the optional is present, but empty, there are no parameters required.
        askInputForQuery(query).ifPresent(stringObjectMap -> {
            QueryExecuteEvent e = new QueryExecuteEvent();
            e.setQuery(query);
            e.setColumnnMap(stringObjectMap);
            EventDispatcher.getInstance().post(e);
        });
    }

    /**
     * Opens a dialog to add a new query, to be added under the {@code root}.
     *
     * @param root The root query.
     */
    public void openNewQueryDialog(final Query root) {
        QueryEditDialog qed = new QueryEditDialog();
        qed.showAndWait();
        if (qed.getResult() == ButtonType.OK) {
            Query theq = qed.getQuery();
            root.getSubQueries().add(theq);

            TreeItem<Query> selected = getSelectionModel().getSelectedItem();
            selected.getChildren().add(new TreeItem<>(theq));
            selected.setExpanded(true);
            refresh();
        }
    }

    /**
     * Opens the {@link QueryEditDialog} based on the current selected {@link Query} in the tree.
     */
    private void openEditSelectedQueryDialog() {
        TreeItem<Query> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        QueryEditDialog qed = new QueryEditDialog();
        qed.initFromQuery(selectedItem.getValue());
        qed.showAndWait();
        if (qed.getResult() == ButtonType.OK) {
            qed.applyTo(selectedItem.getValue());
            refresh();
        }
    }

    /**
     * Moves a {@code source} as a child of {@code target}.
     *
     * @param source The source tree item
     * @param target The new parent tree item where source must reside under.
     */
    private void moveTreeItem(final TreeItem<Query> source, final TreeItem<Query> target) {
        TreeItem<Query> parentOfSource = source.getParent();

        // remove the dragged item from the parent list ...
        parentOfSource.getChildren().remove(source);
        // .. and add the dragged item to the target cell as a child.
        target.getChildren().add(source);

        // We only updated the TreeView's TreeItems. Update the 'backing' query list too.
        Query qParentOfSource = parentOfSource.getValue();
        qParentOfSource.getSubQueries().remove(source.getValue());
        target.getValue().getSubQueries().add(source.getValue());

        // FIXME: there must be a better way to keep the backing tree of queries and the actual
        // TreeItems in sync! I'm currently doing everything twice when it comes to moving, deleting
        // etc.!
    }

    /**
     * Checks if the given {@code possibleDescendant} is a descendant of the {@code ancestor}. If that's the case,
     * you're not allowed to move a TreeItem under a new parent. Example:
     * <pre>
     *   Ancestor
     *     └─ Child
     *          └─ Grandchild
     *               └─ Grand-grand child
     * </pre>
     * Moving {@code Grandchild} (+ {@code Grand-grand child}) under {@code Ancestor} is allowed. However,
     * moving {@code Child} under {@code Grandchild} is not.
     *
     * @param ancestor           The ancestor to check all the descendants from.
     * @param possibleDescendant The possible descendant to check.
     * @return true if the possibleDescendant is indeed a descendant of the ancestor.
     */
    private boolean isDescendant(final TreeItem<Query> ancestor, final TreeItem<Query> possibleDescendant) {
        Deque<TreeItem<Query>> deque = new ArrayDeque<>();
        deque.push(ancestor);
        while (!deque.isEmpty()) {
            TreeItem<Query> next = deque.pop();
            if (next.equals(possibleDescendant)) {
                return true;
            }
            deque.addAll(next.getChildren());
        }

        return false;
    }


    /**
     * Removes the selected query (and its children, ancestors and the whole shebang). This is done by updating the
     * model (the query list) and then forcefully refreshing the whole tree by re-adding the query list again.
     * <p>
     * We'll see how this works out. It can also be done somewhat 'prettier' for the user by deleting the TreeItem, and
     * then also remove it from the backing query list. The improvement on this is that the tree isn't reinitialized
     * (and thus the expand levels are kept the same).
     */
    private void removeSelectedQuery() {
        TreeItem<Query> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        Query q = selectedItem.getValue();

        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        alertConfirm.setTitle("Confirm deletion");
        alertConfirm.setHeaderText(String.format("Delete '%s'?\nThis will also delete %d descendants.", q.getName(), q.getDescendantCount()));
        ((Button) alertConfirm.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
        ((Button) alertConfirm.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
        alertConfirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                TreeItem<Query> parent = selectedItem.getParent();
                // Remove the selected tree item from the parent:
                parent.getChildren().remove(selectedItem);

                Query parentQuery = parent.getValue();
                if (parentQuery == null) {
                    // This is a rooted Query. This does not have a parent.
                    queryList.remove(selectedItem.getValue());
                } else {
                    // There is a parent query.
                    parent.getValue().getSubQueries().remove(selectedItem.getValue());
                }
                refresh();
            }
        });
    }

    /**
     * Contains rendering logic for {@link Query} objects used throughout Gimlet.
     */
    private class QueryConfigurationTreeCell extends TextFieldTreeCell<Query> {

        private ContextMenu menu = new ContextMenu();

        public QueryConfigurationTreeCell() {
            MenuItem menuItemExecute = new MenuItem("Run", Images.MEDIA_PLAY.imageView());
            MenuItem menuItemNew = new MenuItem("New...", Images.PLUS.imageView());
            MenuItem menuItemCut = new MenuItem("Cut", Images.BOLT.imageView());
            MenuItem menuItemCopy = new MenuItem("Copy", Images.BOLT.imageView());
            MenuItem menuItemPaste = new MenuItem("Paste", Images.BOLT.imageView());
            MenuItem menuItemDelete = new MenuItem("Delete...", Images.TRASH.imageView());
            MenuItem menuItemProperties = new MenuItem("Properties...", Images.PENCIL.imageView());

            menu.getItems().addAll(
                    menuItemExecute,
                    new SeparatorMenuItem(),
                    menuItemNew,
                    new SeparatorMenuItem(),
                    menuItemCut,
                    menuItemCopy,
                    menuItemPaste,
                    new SeparatorMenuItem(),
                    menuItemDelete,
                    new SeparatorMenuItem(),
                    menuItemProperties);

            // Funky binding. We bind the 'disabled' property of the menu item, to the boolean property
            // of the editorTabViews's boolean value whether a tab is opened or not.
            menuItemExecute.disableProperty().bind(GimletApp.connectionTabPane.tabSelectedProperty().not());

            menuItemExecute.setOnAction(e -> executeSelectedQuery(getItem()));
            menuItemNew.setOnAction(event -> openNewQueryDialog(getItem()));
            menuItemCut.setOnAction(event -> {
                // todo;
            });
            menuItemCopy.setOnAction(event -> {
                copiedQueryProperty.setValue(new Query(getItem()));
                logger.info("Copied query '{}'", copiedQueryProperty.get().getName());
            });
            menuItemPaste.setOnAction(event -> {
                logger.debug("Pasting {} onto {}", copiedQueryProperty.get(), getSelectionModel().getSelectedItem().getValue());

                // Add the copied query (+ it's copied descendants) to the selected tree item's query:
                TreeItem<Query> selected = getSelectionModel().getSelectedItem();
                selected.getValue().addSubQuery(copiedQueryProperty.get());
                addQuery(selected, Arrays.asList(copiedQueryProperty.get()));

                copiedQueryProperty.setValue(null);
            });
            menuItemPaste.disableProperty().bind(copiedQueryProperty.isNull());
            menuItemDelete.setOnAction(e -> removeSelectedQuery());
            menuItemProperties.setOnAction(e -> openEditSelectedQueryDialog());
        }

        @Override
        public void updateItem(Query item, boolean empty) {
            // super call is required, see documentation.
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setContextMenu(null); // set to null so the 'add root query' context menu can be made visible.
                return;
            }

            if (!isEditing()) {
                this.setContextMenu(menu);
            }

            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));

            // DRAG AND DROP CODE

            // ======================================================
            // When dragging is detected, remember the selected item.
            // ======================================================
            setOnDragDetected(event -> {
                QueryConfigurationTreeCell sourceCell = (QueryConfigurationTreeCell) event.getSource();
                TreeItem<Query> sourceItem = sourceCell.getTreeItem();


                if (sourceItem == null || sourceItem.getParent() == null || sourceItem.getParent().getValue() == null) {
                    return;
                }

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString("herro!");
                // Note: we MUST set content or else the other events are not fired!
                db.setContent(clipboardContent);
                // The clipboard contents can be an object, but must be serializable.
                // We can't afford that (since fields in Query contain unserializable field),
                // therefore we assign a reference to he selected treeitem.
                sourceDraggedItem = getTreeItem();
            });

            // ===================================================
            // When a dragged item is entered on another target...
            // ===================================================
            setOnDragEntered(event -> {
                QueryConfigurationTreeCell targetDropCell = (QueryConfigurationTreeCell) event.getSource();
                TreeItem<Query> targetDropItem = targetDropCell.getTreeItem();

                // If we dragged ourselves unto ourselves ... don't do anything!
                if (sourceDraggedItem == targetDropItem) {
                    return;
                }

                if (targetDropItem == null) {
                    // if the target drop item is null (a tree cell without content),
                    // don't bother displaying to the user that we can't drop here.
                    // This prevents visual feedback when tree branches are collapsed
                    // and the user drags onto empty nodes.
                    return;
                }

                // Check if we can drop the source onto the target, and let the user
                // somehow see that he's not allowed to. TODO: better graphics
                if (!isDescendant(sourceDraggedItem, targetDropItem)) {
                    targetDropCell.setStyle("-fx-background-color: green; -fx-text-fill: white");
                } else {
                    targetDropCell.setStyle("-fx-background-color: red; -fx-text-fill: white");
                }
            });

            // ===================================================
            // Accept transfer modes if moved over a node.
            // ===================================================
            setOnDragOver(event -> {
                event.acceptTransferModes(TransferMode.MOVE);
            });

            // =================================================================
            // When the mouse cursor is exited over a dragged cell, reset styles.
            // =================================================================
            setOnDragExited(event -> {
                QueryConfigurationTreeCell targetDropCell = (QueryConfigurationTreeCell) event.getSource();
                targetDropCell.setStyle(null);
            });

            // =========================================================
            // When we actually dropped a dragged thing, initiate stuff.
            // =========================================================
            setOnDragDropped(event -> {
                QueryConfigurationTreeCell cellTarget = (QueryConfigurationTreeCell) event.getGestureTarget();

                if (cellTarget.getTreeItem() == null) {
                    // We dropped on a TreeCell which does not contain an actual item.
                    event.setDropCompleted(false);
                    return;
                }

                if (!isDescendant(sourceDraggedItem, cellTarget.getTreeItem())) {
                    // We're allowed to move it.
                    moveTreeItem(sourceDraggedItem, cellTarget.getTreeItem());
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
            });
        }
    }
}
