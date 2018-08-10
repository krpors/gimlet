package cruft.wtf.gimlet.ui;

import com.google.common.base.Strings;
import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.conf.GimletProject;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.QueryExecuteEvent;
import cruft.wtf.gimlet.jdbc.ParseResult;
import cruft.wtf.gimlet.ui.dialog.ParamInputDialog;
import cruft.wtf.gimlet.ui.dialog.QueryDialog;
import cruft.wtf.gimlet.ui.dialog.QueryReferenceDialog;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QueryTree extends TreeView<Query> {

    private static Logger logger = LoggerFactory.getLogger(QueryTree.class);

    /**
     * The original assigned query list.
     */
    private List<Query> queryList;

    private ObjectProperty<Query> copiedQueryProperty = new SimpleObjectProperty<>();

    private TreeItem<Query> sourceDraggedItem;

    private GimletProject project;

    private ChangeListener<String> listener = (observable, oldValue, newValue) -> {
        System.out.println("Changed to " + newValue);
        project.updateQueryReference(oldValue, newValue);


    };

    public QueryTree() {
        EventDispatcher.getInstance().register(this);
        setOnKeyPressed(event -> {
            if (isFocused() && event.getCode() == KeyCode.F4 && !event.isAltDown()) {
                openEditSelectedQueryDialog();
            }
        });
    }

    public void setProject(GimletProject project) {
        this.project = project;
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
        MenuItem newRootQuery = new MenuItem("New root query...", Images.PLUS.imageView());
        newRootQuery.setOnAction(event -> openNewRootQueryDialog());
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
            // Add a listener on the name. The name can be used as a reference in other
            // queries, so if the name changes, we must update the references as well!
            q.nameProperty().addListener(listener);
            // Iterate over the references and add them as an item too.
            for (Query q2 : q.findReferencesQueries()) {
                Query copy = new Query(q2);
                copy.nameProperty().bindBidirectional(q2.nameProperty());
                copy.descriptionProperty().bindBidirectional(q2.descriptionProperty());
                copy.setReference(true);
                TreeItem<Query> alskdj = new TreeItem<>(copy);
                qitem.getChildren().add(alskdj);
            }
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
        ParseResult result = ParseResult.parse(query.getContent());
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

    private void openNewRootQueryDialog() {
        QueryDialog qed = new QueryDialog();
        Optional<Query> q = qed.showAndWait();
        q.ifPresent(query -> {
            getRoot().getChildren().add(new TreeItem<>(query));
            queryList.add(query);
        });
    }

    /**
     * Opens a dialog to add a new query, to be added under the {@code root}.
     *
     * @param root The root query.
     */
    public void openNewQueryDialog(final Query root) {
        QueryDialog qed = new QueryDialog();
        Optional<Query> q = qed.showAndWait();
        q.ifPresent(query -> {
            root.getSubQueries().add(query);
            query.setParentQuery(root);
            TreeItem<Query> selected = getSelectionModel().getSelectedItem();
            selected.getChildren().add(new TreeItem<>(query));
            selected.setExpanded(true);
            refresh();
        });
    }

    /**
     * Opens up a dialog to select a reference query.
     *
     * @param item The item to add the reference query to.
     */
    private void openNewQueryRefDialog(final Query item) {
        TreeItem<Query> selectedItem = getSelectionModel().getSelectedItem();

        QueryReferenceDialog dlg = new QueryReferenceDialog(queryList);
        Optional<Query> q = dlg.showAndWait();
        q.ifPresent(query -> {
            item.getReferencedQueries().add(query.getName());

            // Find the last referenced query, and add it after that one. If we don't
            // do this, the tree item will be added after the very last, which may as well
            // be a non-referenced query. If we do it like this, we make sure the references
            // are visible first, and then the custom sub-queries.
            int lastIndexOfRefQuery = 0;
            for (TreeItem<Query> zi : selectedItem.getChildren()) {
                if (zi.getValue().isReference()) {
                    lastIndexOfRefQuery++;
                }
            }

            Query refQuery = new Query(query);
            refQuery.setReference(true);
            selectedItem.getChildren().add(lastIndexOfRefQuery, new TreeItem<>(refQuery));
            refresh();
        });
    }

    /**
     * Opens the {@link QueryDialog} based on the current selected {@link Query} in the tree.
     */
    private void openEditSelectedQueryDialog() {
        TreeItem<Query> selectedItem = getSelectionModel().getSelectedItem();
        // Do NOT act on null items, and do not edit reference queries.
        if (selectedItem == null || selectedItem.getValue().isReference()) {
            return;
        }

        QueryDialog qed = new QueryDialog();
        Optional<Query> q = qed.showEditdialog(selectedItem.getValue());
        q.ifPresent(newQuery -> {
            Query existingQuery = selectedItem.getValue();
            existingQuery.copyFrom(newQuery);
            refresh();
        });
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
     * Moves the current selected TreeItem into the direction given. The movement only applies within the bounds of the
     * children of the parent of the selected node. For example:
     * <pre>
     * Ancestor
     *     └─ Child
     *          └─ Grandchild 1  <---\
     *          └─ Grandchild 2  <----- movement happens between 1, 2 and 3
     *          └─ Grandchild 3  <---/
     * </pre>
     * Grandchild 1 can not be moved 'up' (there is no previous sibling), and Grandchild 3 cannot be moved 'down' (there is no
     * next sibling).
     *
     * @param dir The direction the selected tree node should be moved towards (i.e. it will swap entries in the collections).
     */
    void moveSelectedNode(Direction dir) {
        TreeItem<Query> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        TreeItem<Query> parent = selectedItem.getParent();
        int childCount = parent.getChildren().size();
        int index = parent.getChildren().indexOf(selectedItem);
        boolean isTopLevel = parent.getValue() == null;

        if ((index <= 0 && dir == Direction.UP) || (index >= childCount - 1 && dir == Direction.DOWN)) {
            // moving out of bounds here (up or down) so return prematurely.
            logger.debug("Unable to move node in direction {} (will go out of bounds)", dir);
            return;
        }

        // Swap the TREE items
        Collections.swap(parent.getChildren(), index, index + dir.dir);
        // Swap the backing List
        if (isTopLevel) {
            Collections.swap(queryList, index, index + dir.dir);
        } else {
            Collections.swap(parent.getValue().getSubQueries(), index, index + dir.dir);
        }

        getSelectionModel().select(selectedItem);
    }

    private void removeSelectedRefQuery() {
        TreeItem<Query> selectedTreeItem = getSelectionModel().getSelectedItem();
        if (selectedTreeItem == null) {
            return;
        }

        TreeItem<Query> parentTreeItem = selectedTreeItem.getParent();
        Query parentQuery = parentTreeItem.getValue();

        parentTreeItem.getChildren().remove(selectedTreeItem);
        parentQuery.getReferencedQueries().remove(selectedTreeItem.getValue().getName());

        refresh();
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
        alertConfirm.initOwner(GimletApp.window);
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

        private ContextMenu menuRefs = new ContextMenu();

        public QueryConfigurationTreeCell() {
            MenuItem menuItemExecute = new MenuItem("Run", Images.RUN.imageView());
            MenuItem menuItemNewRoot = new MenuItem("New root query...", Images.PLUS.imageView());
            MenuItem menuItemNew = new MenuItem("New...", Images.PLUS.imageView());
            MenuItem menuItemNewRef = new MenuItem("New reference...", Images.LINK.imageView());
            MenuItem menuItemCut = new MenuItem("Cut", Images.CUT.imageView());
            MenuItem menuItemCopy = new MenuItem("Copy", Images.COPY.imageView());
            MenuItem menuItemPaste = new MenuItem("Paste", Images.PASTE.imageView());
            MenuItem menuItemDelete = new MenuItem("Delete...", Images.TRASH.imageView());
            MenuItem menuItemMoveUp = new MenuItem("Move up", Images.ARROW_UP.imageView());
            MenuItem menuItemMoveDown = new MenuItem("Move down", Images.ARROW_DOWN.imageView());
            MenuItem menuItemProperties = new MenuItem("Properties...", Images.PENCIL.imageView());

            menu.getItems().addAll(
                    menuItemExecute,
                    new SeparatorMenuItem(),
                    menuItemNewRoot,
                    new SeparatorMenuItem(),
                    menuItemNew,
                    menuItemNewRef,
                    new SeparatorMenuItem(),
                    menuItemCut,
                    menuItemCopy,
                    menuItemPaste,
                    new SeparatorMenuItem(),
                    menuItemDelete,
                    new SeparatorMenuItem(),
                    menuItemMoveUp,
                    menuItemMoveDown,
                    new SeparatorMenuItem(),
                    menuItemProperties);

            // Funky binding. We bind the 'disabled' property of the menu item, to the boolean property
            // of the editorTabViews's boolean value whether a tab is opened or not.
            menuItemExecute.disableProperty().bind(ConnectionTabPane.instance.tabSelectedProperty().not());

            menuItemExecute.setOnAction(e -> executeSelectedQuery(getItem()));
            menuItemNewRoot.setOnAction(event -> openNewRootQueryDialog());
            menuItemNew.setOnAction(event -> openNewQueryDialog(getItem()));
            menuItemNewRef.setOnAction(event -> openNewQueryRefDialog(getItem()));
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
            menuItemMoveUp.setOnAction(e -> moveSelectedNode(Direction.UP));
            // TODO: accelerators work, but are still triggered when the tree isn't even visible.
            // menuItemMoveUp.setAccelerator(new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN));
            menuItemMoveDown.setOnAction(e -> moveSelectedNode(Direction.DOWN));
            // menuItemMoveDown.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN));
            menuItemProperties.setOnAction(e -> openEditSelectedQueryDialog());

            menuRefs = new ContextMenu();
            MenuItem itemTest = new MenuItem("Delete", Images.TRASH.imageView());
            itemTest.setOnAction(e -> removeSelectedRefQuery());
            menuRefs.getItems().add(itemTest);
        }

        @Override
        public void updateItem(Query item, boolean empty) {
            // super call is required, see documentation.
            super.updateItem(item, empty);

            setStyle(""); // reset style, always

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
                setGraphic(Images.LINK.imageView());
                setContextMenu(menuRefs);
                setTooltip(new Tooltip(item.getDescription() + "\n\nNote: this query is a reference."));
                return;
            }

            if (item.getParentQuery() == null) {
                setStyle("-fx-base: #c0c0c0");
                setGraphic(Images.MAGNIFYING_GLASS.imageView());
            }
            setTooltip(new Tooltip(item.getDescription()));
            if (!isEditing() && !item.isReference()) {
                this.setContextMenu(menu);
            }


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
                    targetDropCell.setStyle("-fx-base: green;");
                } else {
                    targetDropCell.setStyle("-fx-base: red;");
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
