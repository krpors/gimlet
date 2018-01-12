package cruft.wtf.gimlet.ui;


import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;
import cruft.wtf.gimlet.jdbc.NamedQueryTask;
import cruft.wtf.gimlet.conf.Query;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.Map;

/**
 * This class is a tab where drilldown functionality exists.
 */
public class DrillDownTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(DrillDownTab.class);

    /**
     * Parent {@link ConnectionTab}.
     */
    private final ConnectionTab connectionTab;

    /**
     * The SQL connection. Delegate from the connection tab.
     */
    private Connection connection;

    private TabPane tabPaneResultSets = new TabPane();

    private Node emptyPane;

    /**
     * Creates the {@link DrillDownTab} with the given {@link ConnectionTab} parent.
     *
     * @param connectionTab The tab.
     */
    public DrillDownTab(final ConnectionTab connectionTab) {
        this.connectionTab = connectionTab;
        this.connection = connectionTab.getConnection();

        setText("Drill down");
        setClosable(false);
        setGraphic(Images.COG.imageView());

        emptyPane = createEmptyPane();

        setContent(emptyPane);

        tabPaneResultSets.getTabs().addListener((ListChangeListener<Tab>) c -> {
            if (c.getList().size() <= 0) {
                setContent(emptyPane);
            }
        });
    }

    private Node createEmptyPane() {
        BorderPane pane = new BorderPane();
        pane.setCenter(new Label("Select a query on the left side."));

        return pane;
    }

    public void executeQuery(final Query query, final Map<String, Object> columnMap) {
        logger.debug("Execute drilldown!!!!");

        final DrillResultTable table = new DrillResultTable(this, query);
        final Tab tab = new Tab(query.getName());
        tab.setContent(table);

        NamedQueryTask namedQueryTask = new NamedQueryTask(this.connection, query.getContent(), 100, columnMap);

        namedQueryTask.setOnScheduled(event -> {
            tabPaneResultSets.getTabs().add(tab);
            tabPaneResultSets.getSelectionModel().select(tab);
        });

        namedQueryTask.setOnFailed(event -> {
            // When the query failed, we add a textarea to the tab instead of the table.
            // This textarea contains the stacktrace information.
            StringWriter sw = new StringWriter();
            namedQueryTask.getException().printStackTrace(new PrintWriter(sw));
            TextArea area = new TextArea(String.format("Source query:\n\n%s\n\nStacktrace:\n\n%s", namedQueryTask.getQuery(), sw.toString()));
            area.getStyleClass().add("textarea");
            area.setEditable(false);
            tab.setContent(area);
        });

        namedQueryTask.setOnSucceeded(event -> {
            table.setColumns(namedQueryTask.columnProperty());

            if(namedQueryTask.getValue().size() <= 0) {
                table.setPlaceHolderNoResults();
            } else {
                table.setItems(namedQueryTask.getValue());
            }
            setContent(tabPaneResultSets);
        });

        Thread t = new Thread(namedQueryTask, "Gimlet named query task");
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
