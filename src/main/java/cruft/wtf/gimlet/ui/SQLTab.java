package cruft.wtf.gimlet.ui;


import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;
import cruft.wtf.gimlet.SimpleQueryTask;
import cruft.wtf.gimlet.event.QueryExecutedEvent;
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
import java.sql.Connection;

/**
 * This class is a tab where simple, plain SQL statements can be entered.
 */
public class SQLTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(SQLTab.class);

    /**
     * Parent {@link ConnectionTab}.
     */
    private final ConnectionTab connectionTab;

    /**
     * The SQL connection. Delegate from the connection tab.
     */
    private Connection connection;

    private CheckBox checkMaxRows;

    private TextArea txtQuery = new TextArea("select * from feniks_owner.energy_label where id = 111111");

    private TabPane tabPaneResultSets = new TabPane();


    /**
     * Creates the {@link SQLTab} with the given {@link ConnectionTab} parent.
     *
     * @param connectionTab The tab.
     */
    public SQLTab(final ConnectionTab connectionTab) {
        this.connectionTab = connectionTab;
        this.connection = connectionTab.getConnection();

        setText("SQL");
        setClosable(false);
        setGraphic(Images.PULSE.imageView());

        txtQuery.setWrapText(false);
        txtQuery.setPromptText("Enter any SQL query here");
        txtQuery.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                logger.debug("Executing query...");
                executeQuery();
            }
        });

        BorderPane bpane = new BorderPane();
        checkMaxRows = new CheckBox("Enable max rows 100");
        checkMaxRows.setPadding(new Insets(5, 5, 5, 5));
        bpane.setTop(checkMaxRows);
        bpane.setCenter(txtQuery);

        SplitPane pane = new SplitPane();
        pane.setOrientation(Orientation.VERTICAL);
        pane.setDividerPosition(0, 0.5);
        pane.getItems().add(bpane);
        pane.getItems().add(tabPaneResultSets);

        setContent(pane);
    }

    private void executeQuery() {
        final ResultTable table = new ResultTable();
        final Tab tab = new Tab(txtQuery.getText());
        tab.setContent(table);

        // TODO: parameterize the maxRows properly (via the UI)
        int maxRows = 0;
        if (checkMaxRows.isSelected()) {
            maxRows = 100;
        }
        SimpleQueryTask task = new SimpleQueryTask(this.connection, txtQuery.getText(), maxRows);

        // Task is scheduled and about to start. Add the tab and select it.
        task.setOnScheduled(event -> {
            tabPaneResultSets.getTabs().add(tab);
            tabPaneResultSets.getSelectionModel().select(tab);
        });

        // Task failed:
        task.setOnFailed(event -> {
            // When the query failed, we add a textarea to the tab instead of the table.
            // This textarea contains the stacktrace information.
            StringWriter sw = new StringWriter();
            task.getException().printStackTrace(new PrintWriter(sw));
            TextArea area = new TextArea(sw.toString());
            area.getStyleClass().add("textarea");
            area.setEditable(false);
            tab.setContent(area);
        });

        task.setOnSucceeded(event -> {
            table.setColumns(task.columnProperty());

            if (task.getValue().size() <= 0) {
                table.setPlaceHolderNoResults();
            } else {
                table.setItems(task.getValue());
            }

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
