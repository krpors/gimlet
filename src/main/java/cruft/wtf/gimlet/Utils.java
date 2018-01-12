package cruft.wtf.gimlet;

import cruft.wtf.gimlet.ui.Images;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class with static methods.
 */
public final class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Creates and displays a dialog with {@link Exception} details.
     *
     * @param header    The header text.
     * @param content   The content text.
     * @param exception The exception to display as expandable content.
     */
    public static void showExceptionDialog(String header, String content, Throwable exception) {
        Dialog dialog = new Dialog();
        dialog.getDialogPane().getStylesheets().add("/css/style.css");
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
        textArea.getStyleClass().add("textarea");
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

    /**
     * Utility function to display an error without any stacktrace details.
     *
     * @param header  The header text.
     * @param content The content text.
     */
    public static void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setHeaderText(header);
        alert.showAndWait();
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
