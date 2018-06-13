package cruft.wtf.gimlet.ui.drilldown;

import cruft.wtf.gimlet.jdbc.Column;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.QueryExecutedEvent;
import cruft.wtf.gimlet.jdbc.CachedRowSetTransformer;
import cruft.wtf.gimlet.jdbc.ParseResult;
import cruft.wtf.gimlet.jdbc.task.NamedQueryTask;
import cruft.wtf.gimlet.ui.Images;
import cruft.wtf.gimlet.ui.dialog.ParamInputDialog;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * This tab contains the logic for connecting and executing a named and parameterized query.
 */
public class DrillDownExecutionTab extends Tab {

    private static final Logger logger = LoggerFactory.getLogger(DrillDownExecutionTab.class);

    private final Query query;

    private final DrillDownTab drillDownTab;

    private final DrillResultTable table;

    private Map<String, Object> columnMap;

    private Button btnRerun;

    private Set<ParseResult.Param> uniqueParams;

    public DrillDownExecutionTab(final DrillDownTab drillDownTab, final Query query, final Map<String, Object> columnMap) {
        this.drillDownTab = drillDownTab;
        this.table = new DrillResultTable(query);
        this.query = query;
        this.columnMap = columnMap;

        BorderPane pane = new BorderPane();
        Label lbl = new Label(query.getDescription());
        lbl.setPadding(new Insets(5));

        // Recompile the query so we know the unique parameters. This is used by the re-running
        ParseResult pr = ParseResult.parse(query.getContent());
        uniqueParams = pr.getUniqueParameters();

        btnRerun = new Button();
        btnRerun.setTooltip(new Tooltip("Re-run the query"));
        btnRerun.setGraphic(Images.RUN.imageView());
        btnRerun.setOnAction(event -> openParameterDialog());

        ToggleButton btnLol = new ToggleButton();
        btnLol.setGraphic(Images.TABLE_COLUMN_WIDTH.imageView());
        btnLol.setTooltip(new Tooltip("Toggle fit columns to table width"));
        btnLol.setOnAction(event -> {
            if (btnLol.isSelected()) {
                table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            } else {
                table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
            }
        });

        ToolBar bar = new ToolBar(btnRerun, btnLol);
        VBox box = new VBox(lbl, bar);
        pane.setTop(box);
        pane.setCenter(table);

        textProperty().bind(query.nameProperty());
        setGraphic(Images.CLOCK.imageView());
        setContent(pane);
    }

    /**
     * Opens up the parameters dialog (if uniqueParams is not empty) to request new user input.
     */
    private void openParameterDialog() {
        if (!uniqueParams.isEmpty()) {
            Map<String, Object> prevValues = new HashMap<>();
            // Gather the previously entered values:
            uniqueParams.forEach(s -> prevValues.put(s.getName(), this.columnMap.get(s.getName())));

            // Open dialog with the previous entered values.
            ParamInputDialog dlg = new ParamInputDialog(uniqueParams);
            dlg.prefill(prevValues);
            Optional<Map<String, Object>> opt = dlg.showAndWait();
            opt.ifPresent(stringObjectMap -> {
                // Update the column map so they are shown when rerunning the query.
                this.columnMap = stringObjectMap;
                this.executeQuery(stringObjectMap);
            });
        } else {
            executeQuery();
        }
    }

    /**
     * Executes the query with the columnMap as parameter values.
     */
    public void executeQuery() {
        this.executeQuery(columnMap);
    }

    /**
     * Creates a String with a summary of the exception when the task was in failed state.
     *
     * @param task The {@link NamedQueryTask} in error.
     * @return The String.
     */
    private String createExceptionString(final NamedQueryTask task) {
        if (task.getState() != Worker.State.FAILED) {
            throw new IllegalStateException(String.format("The worker state was in state %s, but expected FAILED", task.getState()));
        }

        // When the query failed, we add a textarea to the tab instead of the table.
        // This textarea contains the stacktrace information.
        StringWriter sw = new StringWriter();
        task.getException().printStackTrace(new PrintWriter(sw));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(task.getException().getMessage()).append("\n\n")
                .append("Source query:\n\n")
                .append(task.getQuery())
                .append("\n\n");

        if (!task.getNamedProperties().isEmpty()) {
            stringBuilder.append("Named parameters given:\n\n");
            task.getNamedProperties().forEach((s, o) -> {
                stringBuilder.append(String.format("\t%s = %s\n", s, o));
            });
        } else {
            stringBuilder.append("The query does not contain named parameters.\n");
        }

        stringBuilder
                .append("\nStacktrace:\n\n")
                .append(sw.toString());

        return stringBuilder.toString();
    }

    /**
     * Execute the query.
     */
    public void executeQuery(final Map<String, Object> columnMap) {
        Connection connection = this.drillDownTab.getConnectionTab().getConnection();

        // Get the max rows from the ConnectionTab (parent, parent). 0 will mean no row limit.
        // TODO: is there an easier way to get this, instead of going up the chain of parents?
        int maxRows = drillDownTab.getConnectionTab().getLimitMaxRows();
        logger.debug("Limiting maximum amount of rows to {} for query '{}'", maxRows, query.getName());

        NamedQueryTask namedQueryTask = new NamedQueryTask(connection, query.getContent(), maxRows, columnMap);

        namedQueryTask.setOnScheduled(event -> {
            btnRerun.setDisable(true);
            table.getItems().clear();
            table.getColumns().clear();
        });

        namedQueryTask.setOnFailed(event -> {
            btnRerun.setDisable(false);

            setGraphic(Images.WARNING.imageView());
            // When the query failed, we add a textarea to the tab instead of the table.
            // This textarea contains the stacktrace information.

            TextArea area = new TextArea(createExceptionString(namedQueryTask));
            area.getStyleClass().add("error-text");
            area.setEditable(false);

            setContent(area);
        });

        namedQueryTask.setOnSucceeded(event -> {
            btnRerun.setDisable(false);
            setGraphic(Images.SPREADSHEET.imageView());

            try (CachedRowSet rowset = namedQueryTask.getValue()) {
                List<Column> columnList = CachedRowSetTransformer.getColumns(rowset);
                ObservableList<ObservableList> data = CachedRowSetTransformer.getData(rowset);

                table.setItems(columnList, data);

                QueryExecutedEvent qee = new QueryExecutedEvent(
                        namedQueryTask.getQuery(),
                        namedQueryTask.getRowCount(),
                        namedQueryTask.getProcessingTime());

                // Posting the event on the bus will result in a new tab
                EventDispatcher.getInstance().post(qee);
            } catch (SQLException e) {
                logger.error("Unhandled exception", e);
            }
        });

        Thread t = new Thread(namedQueryTask, "Gimlet named query task");
        t.setDaemon(true);
        t.start();
    }
}
