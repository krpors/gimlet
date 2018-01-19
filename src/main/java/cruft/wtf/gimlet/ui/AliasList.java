package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.event.ConnectEvent;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

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

        private ContextMenu contextMenu  = new ContextMenu();

        public AliasListCell() {
            // We require separate items for the two different context menu's.
            MenuItem itemConnect = new MenuItem("Connect");
            itemConnect.setGraphic(Images.ACCOUNT_LOGIN.imageView());
            MenuItem newItem = new MenuItem("New");
            newItem.setGraphic(Images.PLUS.imageView());
            MenuItem duplicateItem = new MenuItem("Duplicate");
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setGraphic(Images.TRASH.imageView());
            MenuItem editItem = new MenuItem("Properties...");
            editItem.setGraphic(Images.PENCIL.imageView());

            itemConnect.setOnAction(e -> EventDispatcher.getInstance().post(new ConnectEvent(getItem())));
            newItem.setOnAction(e -> openNewDialog());
            editItem.setOnAction(e -> openEditDialog());
            deleteItem.setOnAction(e -> deleteSelectedAlias());

            // When nothing is selected, disable these items. No use to edit a null item.
            itemConnect.disableProperty().bind(selectedProperty().not());
            duplicateItem.disableProperty().bind(selectedProperty().not());
            deleteItem.disableProperty().bind(selectedProperty().not());
            editItem.disableProperty().bind(selectedProperty().not());

            contextMenu.getItems().add(itemConnect);
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(newItem);
            contextMenu.getItems().add(duplicateItem);
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(deleteItem);
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(editItem);
        }


        @Override
        protected void updateItem(Alias item, boolean empty) {
            super.updateItem(item, empty);

            if (!isEditing()) {
                setContextMenu(contextMenu);
            }

            if (empty || item == null) {
                setText(null);
                setTooltip(null);
                return;
            }

            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));
        }
    }
}

