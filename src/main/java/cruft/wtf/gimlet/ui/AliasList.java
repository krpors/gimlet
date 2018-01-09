package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.event.ConnectEvent;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.List;

public class AliasList extends ListView<Alias> {

    private List<Alias> aliasList;

    public AliasList() {
        setCellFactory(param -> new AliasListCell());
    }

    private void openEditDialog() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        AliasDialog dialog = new AliasDialog();
        dialog.setAliasContent(selected);
        dialog.showAndWait();
        if (dialog.getResult() == ButtonType.OK) {
            dialog.applyTo(selected);
            refresh();
        }
    }


    private void openNewDialog() {
        AliasDialog dialog = new AliasDialog();
        dialog.showAndWait();
        if (dialog.getResult() == ButtonType.OK) {
            Alias a = dialog.createAlias();
            getItems().add(a);
        }
    }

    /**
     * Deletes the selection {@link Alias} from the list.
     */
    private void deleteSelectedAlias() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        getItems().remove(selected);
    }

    /**
     * Sets the Aliases content for this list.
     *
     * @param list
     */
    public void setAliases(final ObservableList<Alias> list) {
        setItems(list);
    }

    /**
     * Custom renderer for every {@link Alias} in the ListView.
     */
    private class AliasListCell extends ListCell<Alias> {

        private ContextMenu contextMenu = new ContextMenu();
        private ContextMenu contextMenu2 = new ContextMenu();

        public AliasListCell() {
            // We require separate items for the two different context menu's.
            MenuItem itemConnect = new MenuItem("Connect");
            itemConnect.setGraphic(Images.ACCOUNT_LOGIN.imageView());
            MenuItem newItem = new MenuItem("New");
            newItem.setGraphic(Images.PLUS.imageView());
            MenuItem editItem = new MenuItem("Edit");
            editItem.setGraphic(Images.PENCIL.imageView());
            MenuItem duplicateItem = new MenuItem("Duplicate");
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setGraphic(Images.TRASH.imageView());

            itemConnect.setOnAction(e -> EventDispatcher.getInstance().post(new ConnectEvent(getItem())));
            newItem.setOnAction(e -> openNewDialog());
            editItem.setOnAction(e -> openEditDialog());
            deleteItem.setOnAction(e -> deleteSelectedAlias());

            contextMenu.getItems().add(itemConnect);
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(newItem);
            contextMenu.getItems().add(editItem);
            contextMenu.getItems().add(duplicateItem);
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(deleteItem);

            // Second context menu. Pops up when right clicked on a null cell.
            MenuItem newItem2 = new MenuItem("New");
            newItem2.setGraphic(Images.PLUS.imageView());
            newItem2.setOnAction(e -> openNewDialog());
            contextMenu2.getItems().add(newItem2);
        }


        @Override
        protected void updateItem(Alias item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setTooltip(null);
                // When we right-clicked on an empty cell, show a different context menu.
                if (!isEditing()) {
                    setContextMenu(contextMenu2);
                }
                return;
            }

            if (!isEditing()) {
                setContextMenu(contextMenu);
            }

            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));
        }
    }
}

