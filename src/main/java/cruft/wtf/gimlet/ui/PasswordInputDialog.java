package cruft.wtf.gimlet.ui;


import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;

/**
 * Simple dialog to enter a password.
 */
public class PasswordInputDialog extends Dialog<String> {

    public PasswordInputDialog(String username) {
        setTitle("Input for query");
        setHeaderText("Specify password for user '" + username + "'");
        setGraphic(Images.LOCK_LOCKED_4X.imageView());

        BorderPane pane = new BorderPane();
        PasswordField field = new PasswordField();
        pane.setCenter(field);

        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setWidth(320);
        setHeight(240);

        setResultConverter(btnType -> {
            if (btnType == ButtonType.OK) {
                // convert cruft to hashmap.
                return field.getText();
            }

            return null;
        });
    }


}
