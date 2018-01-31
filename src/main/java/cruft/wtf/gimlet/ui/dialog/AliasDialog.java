package cruft.wtf.gimlet.ui.dialog;

import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.ui.FormPane;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Optional;

/**
 * The dialog to add/edit an alias in.
 */
public class AliasDialog extends Dialog<Alias> {

    private static Logger logger = LoggerFactory.getLogger(AliasDialog.class);

    private TextField txtName;

    private TextField txtDescription;

    private TextField txtJdbcUrl;

    private ComboBox<String> comboDriverClass;

    private TextField txtUsername;

    private PasswordField txtPassword;

    private CheckBox chkAskForPassword;

    private ColorPicker colorPicker;

    private CheckBox chkDisableColor;

    public AliasDialog() {
        setTitle("Add alias");
        setHeaderText("Specify the values for the alias.");

        getDialogPane().setContent(createContent());

        ButtonType bt = new ButtonType("Test connection");
        getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL,
                bt);

        // Lookup the actual button, and consume the event to prevent the closing
        // of the dialog via the "Test Connection" button.
        Button button = (Button) getDialogPane().lookupButton(bt);
        button.setTooltip(new Tooltip("Test the connection to the datasource using the entered values."));
        button.addEventFilter(ActionEvent.ACTION, event -> {
            testConnection();
            // consume the event so it won't bubble up the chain and close the dialog.
            event.consume();
        });

        setResultConverter(btnType -> {
            if (btnType == ButtonType.OK) {
                return createAliasFromForm();
            }
            return null;
        });
    }

    private Alias createAliasFromForm() {
        Alias alias = new Alias();
        alias.setName(txtName.getText());
        alias.setDescription(txtDescription.getText());
        alias.setUrl(txtJdbcUrl.getText());
        alias.setDriverClass(comboDriverClass.getValue());
        alias.setUser(txtUsername.getText());
        alias.setPassword(txtPassword.getText());
        alias.setColor(Utils.toRgbCode(colorPicker.getValue()));
        alias.setColorDisabled(chkDisableColor.isSelected());
        alias.setAskForPassword(chkAskForPassword.isSelected());
        return alias;
    }

    /**
     * Creates the content of the Scene.
     *
     * @return The scene's content.
     */
    private Parent createContent() {
        FormPane pane = new FormPane();

        txtName = new TextField();
        txtName.setPrefColumnCount(40);
        txtName.setPromptText("Name of this alias");
        pane.add("Name:", txtName);

        txtDescription = new TextField();
        txtDescription.setPromptText("Description of this alias (optional)");
        pane.add("Description:", txtDescription);

        txtJdbcUrl = new TextField();
        txtJdbcUrl.setPromptText("JDBC URL, differs per driver");
        pane.add("JDBC URL:", txtJdbcUrl);

        comboDriverClass = new ComboBox<>();
        comboDriverClass.setEditable(true);
        Enumeration<Driver> ez = DriverManager.getDrivers();
        while (ez.hasMoreElements()) {
            comboDriverClass.getItems().add(ez.nextElement().getClass().getName());
        }

        pane.add("JDBC driver:", comboDriverClass);

        txtUsername = new TextField();
        txtUsername.setPromptText("The username to authenticate with");
        pane.add("Username:", txtUsername);

        txtPassword = new PasswordField();
        txtPassword.setPromptText("The password for the username");

        chkAskForPassword = new CheckBox("Ask for password");
        chkAskForPassword.setTooltip(new Tooltip("Explicitly ask for password when connecting."));

        HBox pwdBox = new HBox(txtPassword, chkAskForPassword);
        HBox.setHgrow(txtPassword, Priority.ALWAYS);
        pwdBox.setSpacing(5);
        pwdBox.setAlignment(Pos.CENTER_LEFT);
        pane.add("Password:", pwdBox);

        txtPassword.disableProperty().bind(chkAskForPassword.selectedProperty());

        chkAskForPassword.setOnAction(event -> {
            if (chkAskForPassword.isSelected()) {
                txtPassword.setText("");
            }
        });

        colorPicker = new ColorPicker();
        chkDisableColor = new CheckBox("Disable color");

        HBox box = new HBox(colorPicker, chkDisableColor);
        box.setSpacing(5);
        box.setAlignment(Pos.CENTER_LEFT);
        pane.add("Tab coloring:", box);

        return pane;
    }

    /**
     * Tests a connection using the filled in values in the form.
     */
    private void testConnection() {
        if (comboDriverClass.getValue() == null || comboDriverClass.getValue().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No driver class is given.", ButtonType.OK).showAndWait();
            return;
        }

        try {
            Class.forName(comboDriverClass.getValue());

            String password = txtPassword.getText();
            if (chkAskForPassword.isSelected()) {
                PasswordInputDialog dlg = new PasswordInputDialog(txtUsername.getText());
                Optional<String> pwd = dlg.showAndWait();
                if (pwd.isPresent()) {
                    password = pwd.get();
                } else {
                    // User cancelled.
                    return;
                }
            }

            Connection c = DriverManager.getConnection(txtJdbcUrl.getText(), txtUsername.getText(), password);
            c.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Press OK to continue.", ButtonType.OK);
            alert.setHeaderText("Connection succeeded!");
            alert.showAndWait();
        } catch (SQLException ex) {
            logger.error("Can't connect", ex);
            Utils.showExceptionDialog(
                    "Connection test failed.",
                    String.format("Failed to connect to '%s'", txtJdbcUrl.getText()),
                    ex);
        } catch (ClassNotFoundException ex) {
            logger.error("Class not found", ex);
            Utils.showExceptionDialog(
                    "Could not instantiate driver.",
                    String.format("Driver class not found: '%s'", comboDriverClass.getValue()),
                    ex);
        }
    }

    /**
     * Sets values of the controls based on the alias.
     *
     * @param alias The alias.
     */
    public Optional<Alias> showEditAlias(final Alias alias) {
        setTitle("Edit alias");
        txtName.setText(alias.getName());
        txtDescription.setText(alias.getDescription());
        txtJdbcUrl.setText(alias.getUrl());
        comboDriverClass.setValue(alias.getDriverClass());
        txtUsername.setText(alias.getUser());
        txtPassword.setText(alias.getPassword());
        colorPicker.setValue(Color.valueOf(alias.getColor()));
        chkDisableColor.setSelected(alias.isColorDisabled());
        chkAskForPassword.setSelected(alias.isAskForPassword());
        return showAndWait();
    }

}

