package cruft.wtf.gimlet;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

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
    public static void showExceptionDialog(String header, String content, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error occurred");
        alert.setHeaderText(header);
        alert.setContentText(content);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        TextArea textArea = new TextArea();
        textArea.setText(sw.toString());
        alert.getDialogPane().setExpandableContent(textArea);

        alert.showAndWait();
    }
}
