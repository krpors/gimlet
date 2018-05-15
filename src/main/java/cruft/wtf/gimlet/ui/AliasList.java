package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.event.ConnectEvent;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import cruft.wtf.gimlet.ui.dialog.AliasDialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;

import java.util.Collections;
import java.util.Optional;

public class AliasList extends ListView<Alias> {

    public AliasList() {
        setCellFactory(param -> new AliasListCell());
        setOnKeyPressed(event -> {
            if (isFocused() && event.getCode() == KeyCode.F4 && !event.isAltDown()) {
                openEditDialog();
            }
        });

        // When no aliases are visible, this context menu will be triggered.
        // When aliases are available, the context menu in the AliasListCell is used.
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemNew = new MenuItem("New", Images.PLUS.imageView());
        menuItemNew.setOnAction(e -> openNewDialog());
        contextMenu.getItems().add(menuItemNew);
        setContextMenu(contextMenu);

        EventDispatcher.getInstance().register(this);
    }

    /**
     * Moves the selected alias up ({@code dir < 0}) or down ({@code dir > 0}) in the list.
     *
     * @param dir The direction. Less than zero to move it up, larger than zero to move it down.
     */
    void moveAlias(Direction dir) {
        final Alias selected = getSelectionModel().getSelectedItem();
        int selectedIndex = getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            // nothing selected.. yet.
            return;
        }

        if (dir == Direction.UP && selectedIndex > 0) {
            // move up
            Collections.swap(getItems(), selectedIndex, selectedIndex - 1);
        }
        if (dir == Direction.DOWN && selectedIndex < getItems().size() - 1) {
            Collections.swap(getItems(), selectedIndex, selectedIndex + 1);
        }

        // reselect the selected.
        getSelectionModel().select(selected);
    }

    private void openEditDialog() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        AliasDialog dialog = new AliasDialog();
        Optional<Alias> optional = dialog.showEditAlias(selected);
        if (optional.isPresent()) {
            // replaceChars the edited one with the new one.
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
        Optional<ButtonType> bt = Utils.showConfirm(String.format("Delete '%s'?", selected.getName()), "Confirm deletion", "Are you sure?");
        bt.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                getItems().remove(selected);
            }
        });
    }

    /**
     * Makes a copy of the selected alias and appends it at the end of the list.
     */
    private void duplicateSelectedAlias() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alias dupe = new Alias(selected);
        getItems().add(dupe);
    }

    /**
     * Custom renderer for every {@link Alias} in the ListView.
     */
    private class AliasListCell extends ListCell<Alias> {

        private ContextMenu contextMenu = new ContextMenu();

        public AliasListCell() {
            // We require separate items for the two different context menu's.
            MenuItem itemConnect = new MenuItem("Connect", Images.ACCOUNT_LOGIN.imageView());
            MenuItem newItem = new MenuItem("New", Images.PLUS.imageView());
            MenuItem duplicateItem = new MenuItem("Duplicate", Images.DUPLICATE.imageView());
            MenuItem deleteItem = new MenuItem("Delete", Images.TRASH.imageView());
            MenuItem editItem = new MenuItem("Properties...", Images.PENCIL.imageView());

            itemConnect.setOnAction(e -> EventDispatcher.getInstance().post(new ConnectEvent(ConnectEvent.Type.INITATED, getItem())));
            newItem.setOnAction(e -> openNewDialog());
            duplicateItem.setOnAction(e -> duplicateSelectedAlias());
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

    @SuppressWarnings("unused")
    @Subscribe
    public void onFileOpened(final FileOpenedEvent foe) {
        setItems(foe.getGimletProject().aliasesProperty());
    }
}

