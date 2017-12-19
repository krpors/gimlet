package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.QueryEditEvent;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class QueryTab extends Tab {

    private final Query query;

    private TextField txtFirstName;
    private TextField txtDescription;
    private TextArea txtContent;

    public QueryTab(final Query query) {
        this.query = query;

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(5);
        pane.setPadding(new Insets(10, 10, 10, 10));

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setMinWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().addAll(col1, col2);

        txtFirstName = new TextField();
        txtFirstName.textProperty().bindBidirectional(query.nameProperty());

        pane.add(new Label("Name:"), 0, 0);
        pane.add(txtFirstName, 1, 0);

        txtDescription = new TextField();
        txtDescription.textProperty().bindBidirectional(query.descriptionProperty());
        pane.add(new Label("Description:"), 0, 1);
        pane.add(txtDescription, 1, 1);

        txtContent = new TextArea();
        txtContent.textProperty().bindBidirectional(query.contentProperty());
        Label label = new Label("Query:");
        GridPane.setValignment(label, VPos.TOP);
        pane.add(label, 0, 2);
        pane.add(txtContent, 1, 2);

        // When closing the tab, be sure to unbind the bidirectional properties.
        setOnCloseRequest(event -> {
            txtFirstName.textProperty().unbind();
            txtDescription.textProperty().unbind();
            textProperty().unbind();
        });

        textProperty().bindBidirectional(query.nameProperty());

        setContent(pane);

    }

    /**
     * Used to determine whether to re-open an already opened tab.
     *
     * @return The {@link Query} that is assigned to this {@link QueryTab}.
     * @see EditorTabView#onQueryEdit(QueryEditEvent)
     */
    public Query getQuery() {
        return query;
    }
}
