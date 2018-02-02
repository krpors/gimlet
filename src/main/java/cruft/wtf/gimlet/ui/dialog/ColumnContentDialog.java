package cruft.wtf.gimlet.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


/**
 * This class is a simple dialog for displaying truncated data.
 */
public class ColumnContentDialog extends Dialog {

    public ColumnContentDialog(String columnName, String content) {
        getDialogPane().getStylesheets().add("/css/style.css");
        setHeaderText("Contents of the column '" + columnName + "'");
        setTitle(columnName);
        setResizable(true);
        getDialogPane().getButtonTypes().add(ButtonType.OK);

        BorderPane pane = new BorderPane();

        TextArea derp = new TextArea(String.valueOf(content));
        derp.setPadding(new Insets(5));
        derp.getStyleClass().add("query-editor");
        derp.setEditable(false);

        CheckBox chkWrap = new CheckBox("Wrap contents");
        derp.wrapTextProperty().bind(chkWrap.selectedProperty());

        Label lblDataType = new Label("Length: " + content.length());

        VBox vbox = new VBox(5);
        vbox.setPadding(new Insets(0, 0, 10, 0));
        vbox.getChildren().add(lblDataType);
        vbox.getChildren().add(chkWrap);

        pane.setTop(vbox);
        pane.setCenter(derp);

        getDialogPane().setContent(pane);
    }
}

