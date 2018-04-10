package cruft.wtf.gimlet.ui;


import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.QueryExecutedEvent;
import cruft.wtf.gimlet.jdbc.task.SimpleQueryTask;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class is a tab where simple, plain SQL statements can be entered.
 */
public class SQLTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(SQLTab.class);

    /**
     * Parent {@link ConnectionTab}.
     */
    private final ConnectionTab connectionTab;

    private CheckBox checkMaxRows;

    private TextArea txtQuery = new TextArea();

    private TabPane tabPaneResultSets = new TabPane();


    /**
     * Creates the {@link SQLTab} with the given {@link ConnectionTab} parent.
     *
     * @param connectionTab The tab.
     */
    public SQLTab(final ConnectionTab connectionTab) {
        this.connectionTab = connectionTab;

        setText("SQL");
        setClosable(false);
        setGraphic(Images.CODE.imageView());
        txtQuery.textProperty().bindBidirectional(connectionTab.getAlias().queryProperty());

        txtQuery.setWrapText(false);
        txtQuery.getStyleClass().add("query-editor");
        txtQuery.setPromptText("Enter any SQL query here");
        txtQuery.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                executeQuery();
            }
        });

        SplitPane pane = new SplitPane();
        pane.setOrientation(Orientation.VERTICAL);
        pane.setDividerPosition(0, 0.30);
        pane.getItems().add(txtQuery);
        pane.getItems().add(tabPaneResultSets);

        setContent(pane);
    }

    /**
     * Executes the query in a JavaFX task.
     */
    private void executeQuery() {
        if (txtQuery.getText() == null) {
            return;
        }

        String query = txtQuery.getText();
        // Check if we selected some text. If so, that's the query we want to run.
        if (txtQuery.getSelection().getLength() != 0) {
            query = txtQuery.getSelectedText();
        }

        String truncated = Utils.truncate(query, 36);
        if (truncated.equals("")) {
            return;
        }

        Derpington derpington = new Derpington(this.connectionTab);

        final Tab tab = new Tab(Utils.truncate(query, 36));
        tab.setGraphic(Images.CLOCK.imageView());
        tab.setContent(derpington);

        int maxRows = connectionTab.getLimitMaxRows();
        logger.debug("Limiting maximum amount of rows to {}", maxRows);

        SimpleQueryTask task = new SimpleQueryTask(this.connectionTab.getConnection(), query, maxRows);

        // Task is scheduled and about to start. Add the tab and select it.
        task.setOnScheduled(event -> {
            tabPaneResultSets.getTabs().add(tab);
            tabPaneResultSets.getSelectionModel().select(tab);
        });

        // Task failed:
        task.setOnFailed(event -> {
            logger.error("Query failed", task.getException());
            // When the query failed, we add a textarea to the tab instead of the table.
            // This textarea contains the stacktrace information.
            StringWriter sw = new StringWriter();
            task.getException().printStackTrace(new PrintWriter(sw));
            TextArea area = new TextArea(sw.toString());
            area.getStyleClass().add("error-text");
            area.setEditable(false);
            tab.setGraphic(Images.WARNING.imageView());
            tab.setContent(area);
        });

        task.setOnSucceeded(event -> {
            tab.setGraphic(Images.SPREADSHEET.imageView());

            derpington.setItems(task.columnProperty(), task.getValue());

            QueryExecutedEvent e = new QueryExecutedEvent();
            e.setQuery(task.getQuery());
            e.setRowCount(task.getRowCount());
            e.setRuntime(task.getProcessingTime());
            EventDispatcher.getInstance().post(e);
        });

        Thread t = new Thread(task, "Gimlet SimpleQueryTask runner");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Closes the tab which is currently selected by a workaround (TabPaneSkin and stuff).
     */
    public void closeSelectedResultTable() {
        // TODO: Extend TabPane with this behaviour, and use that one as the TabPaneResultSets?
        Tab selected = tabPaneResultSets.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TabPaneBehavior b = ((TabPaneSkin) (tabPaneResultSets.getSkin())).getBehavior();
            b.closeTab(selected);
        }
    }
}
