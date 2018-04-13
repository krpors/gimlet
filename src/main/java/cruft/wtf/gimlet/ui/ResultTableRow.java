package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.DataExporter;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a generic row for any result table. It contains a basic context menu with copy/paste actions
 * and all that jazz.
 *
 * @see ResultTable
 * @see cruft.wtf.gimlet.ui.drilldown.DrillResultTable.DrillResultTableRow
 */
public class ResultTableRow extends TableRow<ObservableList> {

    protected ContextMenu contextMenu = new ContextMenu();

    public ResultTableRow() {
        MenuItem itemCopy = new MenuItem("Copy", Images.COPY.imageView());
        contextMenu.getItems().add(itemCopy);

        itemCopy.setOnAction(event -> {
            // The data:
            TableView<ObservableList> tv = getTableView();
            ObservableList<ObservableList> lol = tv.getSelectionModel().getSelectedItems();

            // The column names:
            List<String> colNames = new LinkedList<>();
            getTableView().getColumns().forEach(col -> colNames.add(col.getText()));

            DataExporter.Options opts = new DataExporter.Options();
            opts.columnSeparator = " | ";
            opts.includeColNames = true;
            opts.fitWidth = true;

            // Export string here.
            String yo = DataExporter.exportToBasicString(colNames.toArray(new String[colNames.size()]), lol, opts);

            final ClipboardContent clipboardContent = new ClipboardContent();

            clipboardContent.putString(yo);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });

        setContextMenu(contextMenu);
    }

    @Override
    protected void updateItem(ObservableList item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || item.isEmpty() || empty) {
            return;
        }

        setContextMenu(contextMenu);
    }
}


