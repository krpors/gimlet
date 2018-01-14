package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.QuerySavedEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * The dialog to add/edit a query in.
 */
public class QueryEditDialog extends Stage {

    private Query query;

    private TextField txtName;
    private TextField txtDescription;
    private TextArea  txtQuery;

    private ButtonType result;
    private Button btnOK;
    private Button btnCancel;

    private EventHandler<ActionEvent> eventSave = event -> {
        result = ButtonType.OK;
        close();
    };

    public QueryEditDialog() {
        Parent content = createContent();

        Scene scene = new Scene(content);
        scene.getStylesheets().add("/css/style.css");
        setResizable(true);
        setScene(scene);
        setTitle("Add query");
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

    public void initFromQuery(final Query queryToEdit) {
        setTitle("Edit query");
        txtName.setText(queryToEdit.getName());
        txtQuery.setText(queryToEdit.getContent());
        txtDescription.setText(queryToEdit.getDescription());
    }

    public void applyTo(final Query query) {
        query.setName(txtName.getText());
        query.setDescription(txtDescription.getText());
        query.setContent(txtQuery.getText());
    }

    public Query getQuery() {
        Query q = new Query();
        q.setName(txtName.getText());
        q.setDescription(txtDescription.getText());
        q.setContent(txtQuery.getText());
        return q;
    }

    public ButtonType getResult() {
        return result;
    }

    private Parent createContent() {
        FormPane formPane = new FormPane();

        txtName = new TextField();
        formPane.add("Name:", txtName);

        txtDescription = new TextField();
        formPane.add("Description:", txtDescription);

        txtQuery = new TextArea();
        txtQuery.getStyleClass().add("query-editor");
        // TODO: monospaced font for query
        formPane.add("Query:", txtQuery);

        btnOK = new Button("OK");
        btnOK.setOnAction(eventSave);
        btnCancel = new Button("Cancel");
        btnCancel.setOnAction(event -> close());

        HBox box = new HBox(5, btnOK, btnCancel);
        box.setAlignment(Pos.CENTER_RIGHT);
        formPane.add(box, 1, formPane.rowCounter);

        return new BorderPane(formPane);
    }
}

