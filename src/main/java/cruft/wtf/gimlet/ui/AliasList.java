package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.event.ConnectEvent;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.Collections;
import java.util.Optional;

public class AliasList extends ListView<Alias> {

    private ObservableList<Alias> aliasList;

    public AliasList() {
        setCellFactory(param -> new AliasListCell());
    }

    private void openEditDialog() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        AliasDialog dialog = new AliasDialog();
        Optional<Alias> optional = dialog.showEditAlias(selected);
        if (optional.isPresent()) {
            // replace the edited one with the new one.
            selected.copyFrom(optional.get());
            refresh();
        }

    }

    private void openNewDialog() {
        AliasDialog dialog = new AliasDialog();
        Optional<Alias> optional = dialog.showAndWait();
        optional.ifPresent(alias -> getItems().add(alias));
    }

    /**
     * Deletes the selection {@link Alias} from the list.
     */
    private void deleteSelectedAlias() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Optional<ButtonType> bt = Utils.showConfirm(String.format("Delete '%s'?", selected.getName()), "Confirm deletion", "Ble");
        bt.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                aliasList.remove(selected);
                getItems().remove(selected);
            }
        });
    }

    /**
     * Sets the Aliases content for this list.
     *
     * @param list
     */
    public void setAliases(final ObservableList<Alias> list) {
        this.aliasList = list;
        setItems(list);
    }

    /**
     * Custom renderer for every {@link Alias} in the ListView.
     */
    private class AliasListCell extends ListCell<Alias> {

        private ContextMenu contextMenu = new ContextMenu();

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

