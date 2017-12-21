package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

import java.util.List;

public class AliasList extends ListView<Alias> {


    public AliasList() {
        setCellFactory(param -> new AliasListCell());
    }


    private void openEditDialog() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        AliasEditDialog stage = new AliasEditDialog(selected);
        stage.showAndWait();
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

    public void setAliases(final List<Alias> aliasList) {
        getItems().clear();
        getItems().addAll(aliasList);
    }

    /**
     * Custom renderer for every {@link Alias} in the ListView.
     */
    private class AliasListCell extends ListCell<Alias> {

        private ContextMenu contextMenu = new ContextMenu();
        private ContextMenu contextMenu2 = new ContextMenu();

        public AliasListCell() {
            MenuItem newItem = new MenuItem("New alias...");
            MenuItem newItem2 = new MenuItem("New alias...");
            MenuItem editItem = new MenuItem("Edit");
            MenuItem duplicateItem = new MenuItem("Duplicate");
            MenuItem deleteItem = new MenuItem("Delete");

            editItem.setOnAction(e -> openEditDialog());
            deleteItem.setOnAction(e -> deleteSelectedAlias());

            contextMenu.getItems().add(newItem);
            contextMenu.getItems().add(editItem);
            contextMenu.getItems().add(duplicateItem);
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(deleteItem);

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

