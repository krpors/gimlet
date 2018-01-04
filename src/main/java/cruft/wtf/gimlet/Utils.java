package cruft.wtf.gimlet;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class with static methods.
 */
public final class Utils {

    /**
     * Creates and displays a dialog with {@link Exception} details.
     *
     * @param header    The header text.
     * @param content   The content text.
     * @param exception The exception to display as expandable content.
     */
    public static void showExceptionDialog(String header, String content, Throwable exception) {
        Dialog dialog = new Dialog();
        dialog.setGraphic(Images.DIALOG_ERROR.imageView());
        dialog.setTitle("Error!");
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        Label lbl = new Label(dialog.getContentText());

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setMargin(lbl, new Insets(0, 0, 10, 0));
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane pane = new GridPane();
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.add(lbl, 0, 0);
        pane.add(new Label("Stacktrace:"), 0, 1);
        pane.add(textArea, 0, 2);

        dialog.getDialogPane().setContent(pane);

        dialog.showAndWait();
    }
}
