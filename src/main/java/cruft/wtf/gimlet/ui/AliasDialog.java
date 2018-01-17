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
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * The dialog to add/edit an alias in. Probably refactor because the method I used (see {@link #applyTo(Alias)}
 * and {@link #setAliasContent(Alias)} is jus ugly as fuck.
 */
public class AliasDialog extends Stage {

    private TextField txtName;

    private TextField txtDescription;

    private TextField txtJdbcUrl;

    private ComboBox<String> comboDriverClass;

    private TextField txtUsername;

    private PasswordField txtPassword;

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
        pane.add("Password:", txtPassword);

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
                Connection c = DriverManager.getConnection(txtJdbcUrl.getText(), txtUsername.getText(), txtPassword.getText());
                c.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Press OK to continue.", ButtonType.OK);
                alert.setHeaderText("Connection succeeded!");
                alert.showAndWait();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Utils.showExceptionDialog(
                        "Connection test failed.",
                        String.format("Failed to connect to '%s'", txtJdbcUrl.getText()),
                        ex);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
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
    }

    /**
     * Creates a new {@link Alias} object from the input fields, and returns it. This can be used
     * when the dialog is used to add a new alias instead of editing one.
     *
     * @return The Alias created.
     */
    public Alias createAlias() {
        Alias alias = new Alias();
        alias.setName(txtName.getText());
        alias.setDescription(txtDescription.getText());
        alias.setUrl(txtJdbcUrl.getText());
        alias.setDriverClass(comboDriverClass.getValue());
        alias.setUser(txtUsername.getText());
        alias.setPassword(txtPassword.getText());
        alias.setColor(Utils.toRgbCode(colorPicker.getValue()));
        alias.setColorDisabled(chkDisableColor.isSelected());
        return alias;
    }
}

