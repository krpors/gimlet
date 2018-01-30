package cruft.wtf.gimlet.ui.dialog;

import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.ui.FormPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Optional;

/**
 * The dialog to add/edit a query in.
 */
public class QueryDialog extends Dialog<Query> {

    private Query query;

    private TextField txtName;

    private TextField txtDescription;

    private TextArea txtQuery;

    public QueryDialog() {
        setResizable(true);
        setTitle("Add query");
        setHeaderText("Specify the values for the query.");

        getDialogPane().setPrefWidth(640);
        getDialogPane().setPrefHeight(480);
        getDialogPane().getStylesheets().add("/css/style.css");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(param -> {
            if (param == ButtonType.OK) {
                return createQueryFromFields();
            }

            return null;
        });

        createContent();
    }

    private Query createQueryFromFields() {
        Query q = new Query();
        q.setName(txtName.getText());
        q.setDescription(txtDescription.getText());
        q.setContent(txtQuery.getText());
        return q;
    }

    public Optional<Query> showEditdialog(final Query queryToEdit) {
        setTitle("Edit query");
        txtName.setText(queryToEdit.getName());
        txtQuery.setText(queryToEdit.getContent());
        txtDescription.setText(queryToEdit.getDescription());
        return showAndWait();
    }

    private void createContent() {
        FormPane formPane = new FormPane();

        txtName = new TextField();
        formPane.add("Name:", txtName);

        txtDescription = new TextField();
        formPane.add("Description:", txtDescription);

        txtQuery = new TextArea();
        txtQuery.getStyleClass().add("query-editor");
        GridPane.setVgrow(txtQuery, Priority.ALWAYS);
        formPane.add("Query:", txtQuery);

        getDialogPane().setContent(formPane);
    }
}

