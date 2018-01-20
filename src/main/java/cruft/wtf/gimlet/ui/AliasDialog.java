package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.conf.Alias;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Optional;

/**
 * The dialog to add/edit an alias in. Probably refactor because the method I used (see {@link #applyTo(Alias)}
 * and {@link #setAliasContent(Alias)} is jus ugly as fuck.
 */
public class AliasDialog extends Stage {

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

    private Button btnOK;

    private Button btnCancel;

    private Button btnTestConnection;

    private ButtonType result;

    public AliasDialog() {
        Parent content = createContent();

        Scene scene = new Scene(content);
        setResizable(false);
        setScene(scene);
        setTitle("Add alias");
        initModality(Modality.APPLICATION_MODAL);
        initOwner(GimletApp.mainWindow);
        centerOnScreen();

        // Exit the window (close it) without saving changes.
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                result = ButtonType.CANCEL;
                close();
            }
        });
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

        btnOK = new Button("OK");
        btnOK.setOnAction(event -> {
            result = ButtonType.OK;
            close();
        });
        btnCancel = new Button("Cancel");
        btnCancel.setOnAction(event -> {
            result = ButtonType.CANCEL;
            close();
        });
        btnTestConnection = new Button("Test connection");
        btnTestConnection.setTooltip(new Tooltip("Tests the connection to the given JDBC URL"));
        btnTestConnection.setOnAction(e -> {
            if (comboDriverClass.getValue() == null || comboDriverClass.getValue().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "No driver class is given.", ButtonType.OK).showAndWait();
                return;
            }

            try {
                Class.forName(comboDriverClass.getValue());

                // TODO: password input dialog.
                String password = txtPassword.getText();
                if (chkAskForPassword.isSelected()) {
                    TextInputDialog dlg = new TextInputDialog("");
                    dlg.setHeaderText("Specify password for user '" + txtUsername.getText() + "'");
                    Optional<String> pwd = dlg.showAndWait();
                    if (pwd.isPresent()) {
                        password = pwd.get();
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
        });

        HBox btnBox = new HBox(5, btnOK, btnCancel, btnTestConnection);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        pane.add(btnBox, 1, pane.rowCounter++);

        return pane;
    }

    public ButtonType getResult() {
        return result;
    }

    /**
     * Sets values of the controls based on the alias.
     *
     * @param alias The alias.
     */
    public void setAliasContent(final Alias alias) {
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
    }

    /**
     * Applies the values in the textfields to the given Alias object
     *
     * @param alias The Alias to change.
     */
    public void applyTo(Alias alias) {
        alias.nameProperty().set(txtName.getText());
        alias.descriptionProperty().set(txtDescription.getText());
        alias.urlProperty().set(txtJdbcUrl.getText());
        alias.driverClassProperty().set(comboDriverClass.getValue());
        alias.userProperty().set(txtUsername.getText());
        alias.passwordProperty().set(txtPassword.getText());
        alias.colorProperty().set(Utils.toRgbCode(colorPicker.getValue()));
        alias.colorDisabledProperty().set(chkDisableColor.isSelected());
        alias.askForPasswordProperty().set(chkAskForPassword.isSelected());
    }

}

