package cruft.wtf.gimlet.ui.objects;

import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.jdbc.CachedRowSetTransformer;
import cruft.wtf.gimlet.jdbc.Column;
import cruft.wtf.gimlet.jdbc.task.ObjectLoaderTask;
import cruft.wtf.gimlet.jdbc.task.SimpleQueryTask;
import cruft.wtf.gimlet.jdbc.task.TableColumnMetaDataTask;
import cruft.wtf.gimlet.ui.Images;
import cruft.wtf.gimlet.ui.ResultTable;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This tab contains information about the schema's, tables, columns and all other database
 * metadata.
 */
public class ObjectsTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ObjectsTab.class);

    private Connection connection;

    private Button btnLoadObjects;
    private Button btnCancelLoading;
    private Button btnRemoveEmptySchemas;

    private ProgressIndicator progressIndicator;
    private Label lblLoadingSchema = new Label();
    private Label lblLoadingTable = new Label();

    private TreeView<DatabaseObject> objectTree = new TreeView<>();

    private TabPane tabPane;

    private ObjectsTable table;

    private ResultTable resultTable;

    private BorderPane borderPane;

    private Node nodeLoadingObjects;
    private Node nodeObjects;

    private ObjectLoaderTask taskLoadObjects;

    public ObjectsTab() {
        setText("Database Objects");
        setGraphic(Images.DOCUMENT.imageView());
        setClosable(false);

        borderPane = new BorderPane();

        btnLoadObjects = new Button("", Images.RUN.imageView());
        btnLoadObjects.setTooltip(new Tooltip("(Re)load the database schemas"));
        btnLoadObjects.setOnAction(event -> {
            btnLoadObjects.setDisable(true);
            borderPane.setCenter(nodeLoadingObjects);
            populateTree();
        });

        btnRemoveEmptySchemas = new Button("", Images.TRASH.imageView());
        btnRemoveEmptySchemas.setTooltip(new Tooltip("Remove empty schemas from the view"));
        btnRemoveEmptySchemas.disableProperty().bind(btnLoadObjects.disableProperty());
        btnRemoveEmptySchemas.setOnAction(event -> {
            List<TreeItem<DatabaseObject>> collect = objectTree.getRoot().getChildren()
                    .stream()
                    .filter(databaseObjectTreeItem -> databaseObjectTreeItem.getChildren().isEmpty())
                    .collect(Collectors.toList());
            objectTree.getRoot().getChildren().removeAll(collect);
        });

        ToolBar toolBar = new ToolBar(btnLoadObjects, btnRemoveEmptySchemas);

        nodeLoadingObjects = createLoadPane();
        nodeObjects = createObjectPane();

        borderPane.setTop(toolBar);
        borderPane.setCenter(null);

        setContent(borderPane);
    }

    /**
     * This creates the pane containing the progress indicator and the label which shows the ongoing
     * found schemas and tables.
     */
    private Node createLoadPane() {
        btnCancelLoading = new Button("Cancel");
        btnCancelLoading.setOnAction(event -> {
            taskLoadObjects.cancel();
            borderPane.setCenter(nodeObjects);
        });

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        VBox boxButtons = new VBox(
                btnCancelLoading,
                progressIndicator,
                lblLoadingSchema,
                lblLoadingTable);
        boxButtons.setSpacing(10.0);
        boxButtons.setAlignment(Pos.CENTER);

        return boxButtons;
    }

    /**
     * Creates the object pane, containing the tree on the left side and the table on the right side.
     */
    private Node createObjectPane() {
        objectTree.setCellFactory(param -> new Cell());

        // When selecting a TABLE display something.
        // TODO: this is not done in a task on the background, and may therefore hang the
        objectTree.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            fetchTableMetaData(newVal);
            fetchTablePreviewData(newVal);
        });

        table = new ObjectsTable();
        table.setPlaceholder(null);

        resultTable = new ResultTable();
        resultTable.setPlaceholder(null);

        Tab tabObjectsTable = new Tab("Objects", table);
        Tab tabTableContents = new Tab("Contents", resultTable);

        tabPane = new TabPane(tabObjectsTable, tabTableContents);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        SplitPane splitPaneObjects = new SplitPane(objectTree, tabPane);
        SplitPane.setResizableWithParent(objectTree, false);
        splitPaneObjects.setDividerPosition(0, 0.4);
        return splitPaneObjects;
    }

    /**
     * Sets the connection.
     *
     * @param connection The connection.
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * This methods will fetch table data: column meta-data and a preview of the data in the table.
     */
    private void fetchTableMetaData(final TreeItem<DatabaseObject> newValue) {
        if (newValue == null || newValue.getValue().getType() != DatabaseObject.TABLE) {
            return;
        }

        TableColumnMetaDataTask task = new TableColumnMetaDataTask(this.connection, newValue.getParent().getValue().getTable(), newValue.getValue(), 50);
        task.setOnFailed(event -> {
            System.out.println("failed");
        });
        task.setOnRunning(event -> {
            System.out.println("Running.");
        });
        task.setOnSucceeded(event -> {
            table.getItems().clear();
            System.out.println("Success!");
            table.setItems(task.getValue());
        });

        Thread t = new Thread(task, "Table MetaData Thread");
        t.setDaemon(true);
        t.start();
    }

    private void fetchTablePreviewData(final TreeItem<DatabaseObject> newValue) {
        if (newValue == null || newValue.getValue().getType() != DatabaseObject.TABLE) {
            return;
        }

        logger.debug("Fetching table data for {}", newValue.getValue().getTable());

        // FIXME: HIGHLY unlikely I guess, but SQL injection *may* occur...
        String schema = newValue.getValue().getSchema();
        String table = newValue.getValue().getTable();

        String query = String.format("select * from \"%s\".\"%s\"", schema, table);
        if (schema == null) {
            query = String.format("select * from \"%s\"", table);
        }

        SimpleQueryTask sqt = new SimpleQueryTask(connection, query, 100);
        sqt.setOnSucceeded(event -> {
            try (CachedRowSet rowset = sqt.getValue()) {
                List<Column> columnList = CachedRowSetTransformer.getColumns(rowset);
                ObservableList<ObservableList> data = CachedRowSetTransformer.getData(rowset);
                resultTable.setItems(columnList, data);

            } catch (SQLException ex) {
                logger.error("Error", ex);
                Utils.showExceptionDialog(ex, "Unable to fetch preview data!", "This is probably a bug.");
            }
        });
        Thread t = new Thread(sqt, "Table Preview Data Thread");
        t.setDaemon(true);
        t.start();
    }


    /**
     * Populates the tree by kicking off an {@link ObjectLoaderTask}. The UI is updated with an indeterminate
     * spinner and labels are updated when things are loaded.
     */
    private void populateTree() {
        this.taskLoadObjects = new ObjectLoaderTask(connection);

        taskLoadObjects.loadingSchemaPropertyProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                lblLoadingSchema.setText(newValue);
                lblLoadingTable.setText("");
            });
        });

        taskLoadObjects.loadingTablePropertyProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> lblLoadingTable.setText(newValue));
        });

        taskLoadObjects.setOnScheduled(event -> {
            logger.debug("Task started");
            progressIndicator.setVisible(true);
            borderPane.setCenter(nodeLoadingObjects);
        });

        taskLoadObjects.setOnSucceeded(event -> {
            logger.debug("Task succeeded");
            borderPane.setCenter(nodeObjects);
            objectTree.setRoot(taskLoadObjects.getValue());
            btnLoadObjects.setDisable(false);
        });

        taskLoadObjects.setOnCancelled(event -> {
            logger.debug("Task cancelled");
            btnLoadObjects.setDisable(false);
        });


        taskLoadObjects.setOnFailed(event -> {
            logger.error("Task failed", taskLoadObjects.getException());
        });

        Thread t = new Thread(taskLoadObjects, "Database objects loader");
        t.setDaemon(true);
        t.start();
    }

    /**
     * A tree cell used to render a {@link DatabaseObject}.
     */
    public class Cell extends TextFieldTreeCell<DatabaseObject> {

        @Override
        public void updateItem(DatabaseObject item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                setText(item.getDisplayName());
                switch (item.getType()) {
                    case DatabaseObject.ROOT:
                        setGraphic(Images.FOLDER.imageView());
                        break;
                    case DatabaseObject.SCHEMA:
                        setGraphic(Images.PERSON.imageView());
                        break;
                    case DatabaseObject.TABLE:
                        setGraphic(Images.SPREADSHEET.imageView());
                        break;
                }
            }
        }
    }
}
