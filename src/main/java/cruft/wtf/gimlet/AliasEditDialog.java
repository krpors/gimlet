package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The dialog to add/edit an alias in.
 */
public class AliasEditDialog extends Stage {

    private Alias aliasToEdit;

    private TextField txtName;
    private TextField txtDescription;
    private TextField txtJdbcUrl;
    private TextField txtUsername;
    private PasswordField txtPassword;

    private Button btnOK;
    private Button btnCancel;
    private Button btnTestConnection;

    private EventHandler<ActionEvent> eventSave = event -> {
        aliasToEdit.nameProperty().set(txtName.getText());
        aliasToEdit.descriptionProperty().set(txtDescription.getText());
        aliasToEdit.urlProperty().set(txtJdbcUrl.getText());
        aliasToEdit.userProperty().set(txtUsername.getText());
        aliasToEdit.passwordProperty().set(txtPassword.getText());
        close();
    };

    public AliasEditDialog(Alias aliasToEdit) {
        this.aliasToEdit = aliasToEdit;

        Parent content = createContent();

        Scene scene = new Scene(content);
        setResizable(false);
        setScene(scene);
        setTitle("Edit alias");
//        setWidth(320);
//        setHeight(240);
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

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(5);
        pane.setPadding(new Insets(10, 10, 10, 10));

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setMinWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().addAll(col1, col2);

        txtName = new TextField();
        txtName.setPrefColumnCount(40);
        txtName.setText(aliasToEdit.getName());
        pane.add(new Label("Name:"), 0, 0);
        pane.add(txtName, 1, 0);

        txtDescription = new TextField();
        txtDescription.setText(aliasToEdit.getDescription());
        pane.add(new Label("Description:"), 0, 1);
        pane.add(txtDescription, 1, 1);

        txtJdbcUrl = new TextField();
        txtJdbcUrl.setText(aliasToEdit.getUrl());
        pane.add(new Label("JDBC URL:"), 0, 2);
        pane.add(txtJdbcUrl, 1, 2);

        txtUsername = new TextField();
        txtUsername.setText(aliasToEdit.getUser());
        pane.add(new Label("Username:"), 0, 3);
        pane.add(txtUsername, 1, 3);

        txtPassword = new PasswordField();
        txtPassword.setText(aliasToEdit.getPassword());
        pane.add(new Label("Password:"), 0, 4);
        pane.add(txtPassword, 1, 4);

        btnOK = new Button("OK");
        btnOK.setOnAction(eventSave);
        btnCancel = new Button("Cancel");
        btnCancel.setOnAction(event -> close());
        btnTestConnection = new Button("Test connection");
        btnTestConnection.setTooltip(new Tooltip("Tests the connection to the given JDBC URL"));
        btnTestConnection.setOnAction(e -> {
            try {
                Connection c = DriverManager.getConnection(txtJdbcUrl.getText(), txtUsername.getText(), txtPassword.getText());
                c.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Press OK to continue.", ButtonType.OK);
                alert.setHeaderText("Connection succeeded!");
                alert.showAndWait();
            } catch (SQLException e1) {
                e1.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, e1.getMessage(), ButtonType.OK);
                // TODO: Alert box is quite small to contain the error message.
                alert.setHeaderText("Connection test failed!");
                alert.showAndWait();
            }
        });

        HBox box = new HBox(5, btnOK, btnCancel, btnTestConnection);
        box.setAlignment(Pos.CENTER_RIGHT);
        pane.add(box, 1, 5);

        parentPane.setCenter(pane);

        return parentPane;
    }
}

