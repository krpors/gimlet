package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;

import java.util.List;

public class AliasTable extends ListView<Alias> {


    public AliasTable() {
//        TableColumn<Alias, String> columnOne = new TableColumn<>("Name");
//        columnOne.setCellValueFactory(new PropertyValueFactory<>("name"));
//        TableColumn<Alias, String> columnTwo = new TableColumn<>("Description");
//        columnTwo.setCellValueFactory(new PropertyValueFactory<>("description"));


//        getColumns().setAll(columnOne, columnTwo);

        setCellFactory(param -> new AliasListCell());

        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openEditDialog();
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

