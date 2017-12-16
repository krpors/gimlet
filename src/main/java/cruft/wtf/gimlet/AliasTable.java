package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.conf.AliasConfiguration;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AliasTable extends TableView<Alias> {

    public AliasTable() {
        TableColumn<Alias, String> columnOne = new TableColumn<>("Name");
        columnOne.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Alias, String> columnTwo = new TableColumn<>("Description");
        columnTwo.setCellValueFactory(new PropertyValueFactory<>("description"));

        getColumns().setAll(columnOne, columnTwo);
    }

    public void setAliases(final AliasConfiguration aliasConfiguration) {
        setItems(FXCollections.observableArrayList(aliasConfiguration.getAliases()));
    }
}
