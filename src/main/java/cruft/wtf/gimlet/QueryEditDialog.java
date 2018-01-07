package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.QuerySavedEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
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
    private TextArea txtQuery;

    private Button btnOK;
    private Button btnCancel;

    private EventHandler<ActionEvent> eventSave = event -> {
        query.nameProperty().set(txtName.getText());
        query.descriptionProperty().set(txtDescription.getText());
        query.contentProperty().set(txtQuery.getText());
        QuerySavedEvent qse = new QuerySavedEvent(query);
        EventDispatcher.getInstance().post(qse);
        close();
    };

    public QueryEditDialog(Query query) {
        this.query = query;

        Parent content = createContent();

        Scene scene = new Scene(content);
        setResizable(true);
        setScene(scene);
        setTitle("Edit query");
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
        FormPane formPane = new FormPane();

        txtName = new TextField(query.getName());
        formPane.add("Name:", txtName);

        txtDescription = new TextField(query.getDescription());
        formPane.add("Description:", txtDescription);

        txtQuery = new TextArea(query.getContent());
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

