package cruft.wtf.gimlet.ui;


import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table holding logging events.
 */
public class LogTable extends TableView<String> {

    private static Logger logger = LoggerFactory.getLogger(LogTable.class);


    public LogTable() {
        setEditable(false);
        setTableMenuButtonVisible(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        TableColumn<String, String> columnColName = new TableColumn<>("Message");
        columnColName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));
        columnColName.setMinWidth(120);
        getColumns().add(columnColName);
    }

    public void scrollToEnd() {
        scrollTo(getItems().size());

        // TODO: configurable, and wha'ever. Pick better values.
        // arbitrary value for now. When amount of items exceed 30, remove the first 10.
        if (getItems().size() >= 30) {
            getItems().remove(0, 10);
        }
    }

}
