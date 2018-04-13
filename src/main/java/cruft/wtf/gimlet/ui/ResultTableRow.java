package cruft.wtf.gimlet.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

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
            StringBuilder sb = new StringBuilder();
            TableView<ObservableList> tv = getTableView();
            ObservableList<ObservableList> lol = tv.getSelectionModel().getSelectedItems();
            for (ObservableList rowData : lol) {
                System.out.println(rowData);
                sb.append(rowData.toString());
                sb.append("\n");
            }

            final ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(sb.toString());
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
