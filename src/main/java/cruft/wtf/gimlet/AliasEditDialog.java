package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The dialog to add/edit an alias in.
 */
public class AliasEditDialog extends Stage {

    private Alias alias;

    private TextField txtName;
    private TextField txtDescription;
    private TextField txtJdbcUrl;
    private TextField txtDriverClass;
    private TextField txtUsername;
    private PasswordField txtPassword;

    private Button btnOK;
    private Button btnCancel;
    private Button btnTestConnection;

    private EventHandler<ActionEvent> eventSave = event -> {
        alias.nameProperty().set(txtName.getText());
        alias.descriptionProperty().set(txtDescription.getText());
        alias.urlProperty().set(txtJdbcUrl.getText());
        alias.urlProperty().set(txtDriverClass.getText());
        alias.userProperty().set(txtUsername.getText());
        alias.passwordProperty().set(txtPassword.getText());
        close();
    };

    public AliasEditDialog(Alias aliasToEdit) {
        this.alias = aliasToEdit;

        Parent content = createContent();

        Scene scene = new Scene(content);
        setResizable(false);
        setScene(scene);
        setTitle("Edit alias");
        initModality(Modality.APPLICATION_MODAL);
        initOwner(GimletApp.mainWindow);
        centerOnScreen();

        // Exit the window (close it) without saving changes.
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });
    }

    private Parent createContent() {

        BorderPane parentPane = new BorderPane();

        FormPane pane = new FormPane();

//        GridPane pane = new GridPane();
//        pane.setHgap(10);
//        pane.setVgap(5);
//        pane.setPadding(new Insets(10, 10, 10, 10));
//
//        ColumnConstraints col1 = new ColumnConstraints();
//        ColumnConstraints col2 = new ColumnConstraints();
//        col1.setMinWidth(50);
//        col2.setHgrow(Priority.ALWAYS);
//        pane.getColumnConstraints().addAll(col1, col2);

        txtName = new TextField();
        txtName.setPrefColumnCount(40);
        txtName.setText(alias.getName());
        txtName.setPromptText("Name of this alias");
        pane.add("Name:", txtName);

        txtDescription = new TextField();
        txtDescription.setText(alias.getDescription());
        txtDescription.setPromptText("Description of this alias (optional)");
        pane.add("Description:", txtDescription);

        txtJdbcUrl = new TextField();
        txtJdbcUrl.setText(alias.getUrl());
        txtJdbcUrl.setPromptText("JDBC URL, differs per driver");
        pane.add("JDBC URL:", txtJdbcUrl);

        txtDriverClass = new TextField();
        txtDriverClass.setText(alias.getDriverClass());
        txtDriverClass.setPromptText("JDBC driver class, e.g. org.hsqldb.jdbc.JDBCDriver");
        pane.add("JDBC driver:", txtDriverClass);

        txtUsername = new TextField();
        txtUsername.setText(alias.getUser());
        txtUsername.setPromptText("The username to authenticate with");
        pane.add("Username:", txtUsername);

        txtPassword = new PasswordField();
        txtPassword.setText(alias.getPassword());
        txtPassword.setPromptText("The password for the username");
        pane.add("Password:", txtPassword);

        btnOK = new Button("OK");
        btnOK.setOnAction(eventSave);
        btnCancel = new Button("Cancel");
        btnCancel.setOnAction(event -> close());
        btnTestConnection = new Button("Test connection");
        btnTestConnection.setTooltip(new Tooltip("Tests the connection to the given JDBC URL"));
        btnTestConnection.setOnAction(e -> {
            if (txtDriverClass.getText() == null || txtDriverClass.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "No driver class is given.", ButtonType.OK).showAndWait();
                return;
            }

            try {
                Class.forName(txtDriverClass.getText());
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
                        String.format("Driver class not found: '%s'", txtDriverClass.getText()),
                        ex);
            }
        });

        HBox box = new HBox(5, btnOK, btnCancel, btnTestConnection);
        box.setAlignment(Pos.CENTER_RIGHT);
        pane.add(box, 1, pane.rowCounter++);

        parentPane.setCenter(pane);

        return parentPane;
    }
}

