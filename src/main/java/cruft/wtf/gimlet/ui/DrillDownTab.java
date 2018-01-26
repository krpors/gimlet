package cruft.wtf.gimlet.ui;


import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.QueryExecutedEvent;
import cruft.wtf.gimlet.jdbc.NamedQueryTask;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    private TabPane tabPaneResultSets;

    /**
     * The content pane is a stack pane containing the contents. When no result is shown (i.e. no tabs visible)  an
     * 'empty' pane is shown with some text provided.
     */
    private StackPane contentPane;

    private Node emptyPane;

    /**
     * Creates the {@link DrillDownTab} with the given {@link ConnectionTab} parent.
     *
     * @param connectionTab The tab.
     */
    public DrillDownTab(final ConnectionTab connectionTab) {
        this.connectionTab = connectionTab;

        setText("Drill down");
        setClosable(false);
        setGraphic(Images.MAGNIFYING_GLASS.imageView());

        contentPane = new StackPane();
        emptyPane = createEmptyPane();

        tabPaneResultSets = new TabPane();
        tabPaneResultSets.setVisible(false);

        contentPane.getChildren().add(emptyPane);
        contentPane.getChildren().add(tabPaneResultSets);

        setContent(contentPane);

        tabPaneResultSets.getTabs().addListener((ListChangeListener<Tab>) c -> {
            boolean noTabs = c.getList().size() <= 0;
            emptyPane.setVisible(noTabs);
            tabPaneResultSets.setVisible(!noTabs);
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
        tab.setGraphic(Images.CLOCK.imageView());

        NamedQueryTask namedQueryTask = new NamedQueryTask(
                this.connectionTab.getConnection(),
                query.getContent(),
                100,
                columnMap);

        namedQueryTask.setOnScheduled(event -> {
            tabPaneResultSets.getTabs().add(tab);
            tabPaneResultSets.getSelectionModel().select(tab);
        });

        namedQueryTask.setOnFailed(event -> {
            tab.setGraphic(Images.WARNING.imageView());
            // When the query failed, we add a textarea to the tab instead of the table.
            // This textarea contains the stacktrace information.
            StringWriter sw = new StringWriter();
            namedQueryTask.getException().printStackTrace(new PrintWriter(sw));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("Source query:\n\n")
                    .append(namedQueryTask.getQuery())
                    .append("\n\n");

            if (!namedQueryTask.getNamedProperties().isEmpty()) {
                stringBuilder.append("Named parameters given:\n\n");
                namedQueryTask.getNamedProperties().forEach((s, o) -> {
                    stringBuilder.append(String.format("\t%s = %s\n", s, o));
                });
            } else {
                stringBuilder.append("The query does not contain named parameters.\n");
            }

            stringBuilder
                    .append("\nStacktrace:\n\n")
                    .append(sw.toString());
            TextArea area = new TextArea(stringBuilder.toString());
            area.getStyleClass().add("textarea");
            area.setEditable(false);
            tab.setContent(area);
        });

        namedQueryTask.setOnSucceeded(event -> {
            tab.setGraphic(Images.SPREADSHEET.imageView());
            table.setColumns(namedQueryTask.columnProperty());

            if (namedQueryTask.getRowCount() <= 0) {
                table.setPlaceHolderNoResults();
            } else {
                table.setItems(namedQueryTask.getValue());
            }

            QueryExecutedEvent qee = new QueryExecutedEvent();
            qee.setQuery(namedQueryTask.getQuery());
            qee.setRuntime(namedQueryTask.getProcessingTime());
            qee.setRowCount(namedQueryTask.getRowCount());
            EventDispatcher.getInstance().post(qee);
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
