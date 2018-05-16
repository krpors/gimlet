package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.Configuration;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.ui.dialog.ColumnContentDialog;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.Optional;

public class ResultSetCell extends TextFieldTableCell<ObservableList, Object> {

    public ResultSetCell() {
    }

    @Override
    public void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        // a Cell seems to be reused, so clear any state which may have been set.
        getStyleClass().remove("null");
        getStyleClass().remove("truncate");
        setOnMouseClicked(null);

        if (empty) {
            return;
        }

        if (item == null) {
            // add a style class so it's easy to see it's nulled (from the database).
            getStyleClass().add("null");
            // set a string indicator. The text is centered via CSS.
            setText("<NULL>");
            return;
        }

        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ColumnContentDialog d = new ColumnContentDialog(getTableColumn().getText(), String.valueOf(item));
                d.showAndWait();
            }
        });

        // If the cell content is larger than 32 chars, abbreviate it and mark it as such.
        // User should be able to double click on the cell to display its full data.
        // Cells which contain Strings are applicable for truncation only. So check that.
        if (!(item.getClass().equals(String.class))) {
            return;
        }

        String s = item.toString().replace("\n", "\\n").replace("\r", "\\r").trim();

        Optional<Integer> tsize = Configuration.getInstance().getIntegerProperty(Configuration.Key.TRUNCATE_SIZE);
        int truncateSize = tsize.orElse(32);

        setText(s);

        if (s.length() >= truncateSize) {
            getStyleClass().add("truncate");

            Tooltip t = new Tooltip("The contents of this cell are truncated.\nDouble click to see the full contents.");
            t.setGraphic(Images.WARNING.imageView());
            setTooltip(t);
            setText(Utils.truncate(s, truncateSize));
        }
    }
}
