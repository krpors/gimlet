package cruft.wtf.gimlet;

import cruft.wtf.gimlet.ui.Images;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Utility class with static methods.
 */
public final class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Text formatter to allow numbers only. Used in text fields to only accept numbers.
     */
    public static TextFormatter<String> createFormatter() {
        return new TextFormatter<>(change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        });
    }

    /**
     * Creates and displays a dialog with {@link Exception} details.
     *
     * @param exception The exception to display as expandable content.
     * @param header    The header text.
     * @param content   The content text.
     * @param va
     */
    public static void showExceptionDialog(Throwable exception, String header, String content, Object... va) {
        Dialog dialog = new Dialog();
        dialog.initOwner(GimletApp.window);
        dialog.getDialogPane().getStylesheets().add("/css/style.css");
        dialog.setGraphic(Images.DIALOG_ERROR.imageView());
        dialog.setTitle("Error!");
        dialog.setHeaderText(header);
        dialog.setContentText(String.format(content, va));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        Label lbl = new Label(dialog.getContentText());

        TextArea textArea = new TextArea(sw.toString());
        textArea.getStyleClass().add("error-text");
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

    public static String toRgbCode(final Color color) {
        if (color == null) {
            return null;
        }
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Utility function to display an error without any stacktrace details.
     *
     * @param header  The header text.
     * @param content The content text.
     */
    public static void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.initOwner(GimletApp.window);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    /**
     * Shows information.
     *
     * @param header  The header text.
     * @param content The content text.
     */
    public static void showInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.initOwner(GimletApp.window);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog.
     * @param header
     * @param title
     * @param content
     * @return
     */
    public static Optional<ButtonType> showConfirm(String header, String title, String content) {
        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, content);
        alertConfirm.initOwner(GimletApp.window);
        alertConfirm.setTitle(title);
        alertConfirm.setHeaderText(header);
        ((Button) alertConfirm.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
        ((Button) alertConfirm.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
        return alertConfirm.showAndWait();
    }

    /**
     * Truncates a string to a certain length, followed by and ellipses (as three dots). Any newlines and such are
     * removed.
     *
     * @param s      The String to truncate.
     * @param maxlen The maximum length.
     * @return The truncated string ({@code blah blah blah ...} or the string itself if it's smaller than maxlen.
     */
    public static String truncate(String s, int maxlen) {
        String trim = s
                .trim()
                .replace('\n', ' ')
                .replace('\r', ' ');
        if (trim.length() >= maxlen) {
            return trim.substring(0, maxlen - 3).concat("...");
        }
        return trim;
    }

    public String fileExtension(final File file) {
        int idx = file.getName().lastIndexOf('.');
        if (idx < 0) {
            return "";
        }

        return file.getName().substring(idx);
    }

    /**
     *
     */
    public static void close(final Statement statement) throws SQLException {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.error("Still unable to close statement", e);
                throw e;
            }
        }
    }

    public static void close(final ResultSet rs) throws SQLException {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Still unable to close resultset", e);
                throw e;
            }
        }
    }

}
