package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.DataConverter;
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

/**
 * This is a generic row for any result table. It contains a basic context menu with copy/paste actions
 * and all that jazz.
 *
 * @see ResultTable
 * @see cruft.wtf.gimlet.ui.drilldown.DrillResultTable.DrillResultTableRow
 */
public class ResultTableRow extends TableRow<ObservableList> {

    private static final Logger logger = LoggerFactory.getLogger(ResultTableRow.class);

    protected ContextMenu contextMenu = new ContextMenu();

    public ResultTableRow() {
        Menu subMenu = new Menu("Copy");

        MenuItem itemCopy = new MenuItem("Copy as plain text", Images.COPY.imageView());
        MenuItem itemCopyAsPlainHtml = new MenuItem("Copy as plain HTML text", Images.CODE.imageView());
        MenuItem itemCopyAsHtml = new MenuItem("Copy as HTML", Images.SPREADSHEET.imageView());

        subMenu.getItems().addAll(
                itemCopy,
                itemCopyAsPlainHtml,
                itemCopyAsHtml);

        contextMenu.getItems().add(subMenu);

        final ClipboardContent clipboardContent = new ClipboardContent();

        itemCopy.setOnAction(event -> {
            DataConverter.Options opts = new DataConverter.Options();
            opts.columnSeparator = " | ";
            opts.includeColNames = true;
            opts.fitWidth = true;

            // Export string here.
            String yo = DataConverter.convertToText(getColumnNames(), getTableData(), opts);

            clipboardContent.putString(yo);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
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


