package cruft.wtf.gimlet.ui.dialog;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.ui.FormPane;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * The dialog to add/edit a query in.
 */
public class QueryDialog extends Dialog<Query> {

    private Query query;

    private TextField txtName;

    private TextField txtDescription;

    private TextArea txtQuery;

    private ColorPicker colorPicker;

    private CheckBox chkDisableColor;

    public QueryDialog() {
        initOwner(GimletApp.window);
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
        q.setColor(Utils.toRgbCode(colorPicker.getValue()));
        q.setColorDisabled(chkDisableColor.isSelected());
        return q;
    }

    public Optional<Query> showEditdialog(final Query queryToEdit) {
        setTitle("Edit query");
        txtName.setText(queryToEdit.getName());
        txtQuery.setText(queryToEdit.getContent());
        txtDescription.setText(queryToEdit.getDescription());
        colorPicker.setValue(Color.valueOf(queryToEdit.getColor()));
        chkDisableColor.setSelected(queryToEdit.isColorDisabled());
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

        colorPicker = new ColorPicker();
        chkDisableColor = new CheckBox("Disable color");

        HBox box = new HBox(colorPicker, chkDisableColor);
        box.setSpacing(5);
        box.setAlignment(Pos.CENTER_LEFT);
        formPane.add("Query coloring:", box);

        getDialogPane().setContent(formPane);

        setOnShown(event -> Platform.runLater(() -> txtName.requestFocus()));
    }
}

