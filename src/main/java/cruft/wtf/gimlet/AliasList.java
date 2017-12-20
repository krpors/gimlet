package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.event.ConnectEvent;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;

import java.util.List;

public class AliasList extends ListView<Alias> {


    public AliasList() {
        setCellFactory(param -> new AliasListCell());

        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                EventDispatcher.getInstance().post(new ConnectEvent(getSelectionModel().getSelectedItem()));
            }
        });
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                openEditDialog();
            }
        });
    }

    private void openEditDialog() {
        Alias selected = getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        AliasEditDialog stage = new AliasEditDialog(selected);
        stage.showAndWait();
    }

    public void setAliases(final List<Alias> aliasList) {
        setItems(FXCollections.observableArrayList(aliasList));
    }

    private static class AliasListCell extends ListCell<Alias> {
        @Override
        protected void updateItem(Alias item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                return;
            }

            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));
        }
    }
}

