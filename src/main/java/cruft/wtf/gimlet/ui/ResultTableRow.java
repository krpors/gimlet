package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.ui.dialog.CopyAsDialog;
import cruft.wtf.gimlet.util.DataConverter;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * This is a generic row for any result table. It contains a basic context menu with copy/paste actions
 * and all that jazz.
 *
 * @see ResultTable
 * @see cruft.wtf.gimlet.ui.drilldown.DrillResultTableRow
 */
public class ResultTableRow extends TableRow<ObservableList> {

    private static final Logger logger = LoggerFactory.getLogger(ResultTableRow.class);

    protected ContextMenu contextMenu = new ContextMenu();

    protected Menu menuCopy;

    public ResultTableRow() {
        menuCopy = new Menu("Copy");

        MenuItem itemCopy = new MenuItem("As plain text...", Images.COPY.imageView());
        MenuItem itemCopyAsPlainHtml = new MenuItem("As plain HTML", Images.CODE.imageView());
        MenuItem itemCopyAsHtml = new MenuItem("As HTML", Images.SPREADSHEET.imageView());

        menuCopy.getItems().addAll(
                itemCopy,
                itemCopyAsPlainHtml,
                itemCopyAsHtml);

        contextMenu.getItems().add(menuCopy);

        final ClipboardContent clipboardContent = new ClipboardContent();

        itemCopy.setOnAction(event -> {
            CopyAsDialog dlg = new CopyAsDialog(getColumnNames(), getTableData());
            Optional<String> result = dlg.showAndWait();

            result.ifPresent(s -> {
                clipboardContent.putString(s);
                Clipboard.getSystemClipboard().setContent(clipboardContent);
                logger.info("Copied {} rows to the system clipboard", getTableData().size());
            });
        });

        itemCopyAsPlainHtml.setOnAction(event -> {
            String what = DataConverter.convertToHtml(getColumnNames(), getTableData());
            clipboardContent.putString(what);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });

        itemCopyAsHtml.setOnAction(event -> {
            String what = DataConverter.convertToHtml(getColumnNames(), getTableData());
            clipboardContent.putHtml(what);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
            logger.debug("Copied {} rows as HTML", getTableData().size());
        });

        setContextMenu(contextMenu);
    }

    private ObservableList<ObservableList> getTableData() {
        return getTableView().getSelectionModel().getSelectedItems();
    }

    private List<String> getColumnNames() {
        List<String> colNames = new LinkedList<>();
        getTableView().getColumns().forEach(col -> colNames.add(col.getText()));
        return colNames;
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


