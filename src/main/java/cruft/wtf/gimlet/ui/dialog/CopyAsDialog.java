package cruft.wtf.gimlet.ui.dialog;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.ui.FormPane;
import cruft.wtf.gimlet.util.DataConverter;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This dialog can be used to copy selected rows of a table (by giving a list of column names, and a list of lists as
 * the table data).
 */
public class CopyAsDialog extends Dialog<String> {

    private static Logger logger = LoggerFactory.getLogger(CopyAsDialog.class);

    private CheckBox chkIncludeColNames;

    private CheckBox chkFitWidth;

    private TextField txtColumnSeparator;

    private TextArea txtPreview;

    private DataConverter.Options options;

    private List<String> columnNames;

    private ObservableList<ObservableList> data;

    public CopyAsDialog(List<String> columnNames, ObservableList<ObservableList> data) {
        this.columnNames = columnNames;
        this.data = data;

        initOwner(GimletApp.window);
        setTitle("Advanced copy");
        setHeaderText("Copy rows");

        options = new DataConverter.Options();
        options.setIncludeColNames(true);
        options.setFitWidth(true);
        options.setColumnSeparator(",");

        getDialogPane().setContent(createContent());
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        setResultConverter(param -> {
            if (param == ButtonType.OK) {
                return DataConverter.convertToText(columnNames, data, options);
            }
            return null;
        });
    }

    private Node createContent() {
        chkIncludeColNames = new CheckBox("Include column names");
        chkIncludeColNames.selectedProperty().bindBidirectional(options.includeColNamesProperty());
        chkIncludeColNames.setOnAction(event -> refreshPreview());

        chkFitWidth = new CheckBox("Fit width");
        chkFitWidth.selectedProperty().bindBidirectional(options.fitWidthProperty());
        chkFitWidth.setOnAction(event -> refreshPreview());

        txtColumnSeparator = new TextField();
        txtColumnSeparator.textProperty().bindBidirectional(options.columnSeparatorProperty());
        txtColumnSeparator.setOnKeyReleased(event -> refreshPreview());
        txtColumnSeparator.setTooltip(new Tooltip("Separator for each column. '\\t' can be used for a tab separator."));

        Label lblPreview = new Label("Preview");

        txtPreview = new TextArea();
        txtPreview.setStyle("-fx-font-family: monospace");
        txtPreview.setEditable(false);

        FormPane pane = new FormPane();
        pane.add("Column separator:", txtColumnSeparator);

        VBox box = new VBox(
                chkIncludeColNames,
                chkFitWidth,
                pane,
                lblPreview,
                txtPreview
        );
        box.setSpacing(5.0);

        refreshPreview();

        return box;
    }

    private void refreshPreview() {
        int maxIndex = Math.min(9, data.size());

        List<ObservableList> sublist = data.subList(0, maxIndex);
        txtPreview.setText(DataConverter.convertToText(columnNames, sublist, options));
    }

}

