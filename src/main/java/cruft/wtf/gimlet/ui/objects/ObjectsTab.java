package cruft.wtf.gimlet.ui.objects;

import cruft.wtf.gimlet.jdbc.ObjectLoaderTask;
import cruft.wtf.gimlet.jdbc.SqlType;
import cruft.wtf.gimlet.ui.Images;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This tab contains information about the schema's, tables, columns and all other database
 * metadata.
 */
public class ObjectsTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ObjectsTab.class);

    private Connection connection;

    private Button btnLoadObjects;

    private Label lblLoadingSchema = new Label();

    private Label lblLoadingTable = new Label();

    private ProgressIndicator bar;

    private TreeView<DatabaseObject> objectTree = new TreeView<>();

    private VBox boxButtons;

    private ObjectsTable table;

    private BorderPane paneObjects;

    private Button btnRemoveEmptySchemas;

    private StackPane stackPane;

    private ObjectLoaderTask taskLoadObjects;

    private EventHandler<ActionEvent> handlerCancel;

    private EventHandler<ActionEvent> handlerLoad;

    public ObjectsTab() {
        setText("Database Objects");
        setGraphic(Images.DOCUMENT.imageView());
        setClosable(false);

        initHandlers();

        createObjectPane();
        createLoadPane();

        stackPane = new StackPane(boxButtons);
        setContent(stackPane);
    }

    private void createLoadPane() {
        bar = new ProgressIndicator();
        bar.setVisible(false);

        btnLoadObjects = new Button("Load database objects", Images.RUN.imageView());
        btnLoadObjects.setOnAction(handlerLoad);

        boxButtons = new VBox(
                btnLoadObjects,
                bar,
                lblLoadingSchema,
                lblLoadingTable);
        boxButtons.setSpacing(10.0);
        boxButtons.setAlignment(Pos.CENTER);
    }

    /**
     * Creates the object pane.
     */
    private void createObjectPane() {
        objectTree.setCellFactory(param -> new Cell());

        // When selecting a TABLE display something.
        // TODO: this is not done in a task on the background, and may therefore hang the
        objectTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            table.getItems().clear();

            DatabaseObject obj = newValue.getValue();
            if (obj.getType() == DatabaseObject.TABLE) {
                DatabaseObject parent = newValue.getParent().getValue();

                try {
                    DatabaseMetaData dmd = connection.getMetaData();
                    ResultSet rs = dmd.getColumns(null, parent.getName(), obj.getName(), "%");

                    // Get primary keys of the table.
                    ResultSet pks = dmd.getPrimaryKeys(null, parent.getName(), obj.getName());
                    Set<String> setColumnPks = new HashSet<>();
                    while (pks.next()) {
                        setColumnPks.add(pks.getString("COLUMN_NAME"));
                    }
                    pks.close();

                    ObservableList<ObjectsTableData> derp = FXCollections.observableArrayList();

                    int ordinal = 0;
                    while (rs.next()) {
                        ObjectsTableData data = new ObjectsTableData();
                        data.setOrdinalPosition(ordinal++);
                        data.setColumnName(rs.getString("COLUMN_NAME"));
                        data.setDataType(SqlType.getType(rs.getInt("DATA_TYPE")));
                        data.setTypeName(rs.getString("TYPE_NAME"));
                        data.setColumnSize(rs.getInt("COLUMN_SIZE"));
                        data.setNullable(rs.getInt("NULLABLE") != 0);
                        data.setPrimaryKey(setColumnPks.contains(rs.getString("COLUMN_NAME")));
                        derp.add(data);
                    }

                    table.setItems(derp);

                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        table = new ObjectsTable();
        table.setPlaceholder(null);

        SplitPane splitPaneObjects = new SplitPane(objectTree, table);
        SplitPane.setResizableWithParent(objectTree, false);
        splitPaneObjects.setDividerPosition(0, 0.4);

        btnRemoveEmptySchemas = new Button("Remove empty schemas");
        btnRemoveEmptySchemas.setGraphic(Images.TRASH.imageView());
        btnRemoveEmptySchemas.setOnAction(event -> {
            List<TreeItem<DatabaseObject>> collect = objectTree.getRoot().getChildren()
                    .stream()
                    .filter(databaseObjectTreeItem -> databaseObjectTreeItem.getChildren().isEmpty())
                    .collect(Collectors.toList());
            objectTree.getRoot().getChildren().removeAll(collect);
            btnRemoveEmptySchemas.setDisable(true);
        });
        HBox lolbox = new HBox(btnRemoveEmptySchemas);
        lolbox.setPadding(new Insets(10));

        paneObjects = new BorderPane();
        paneObjects.setTop(lolbox);
        paneObjects.setCenter(splitPaneObjects);

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
     * Initialize event handlers for the buttons.
     */
    private void initHandlers() {
        // Initialize some actions for swapping loading/cancelling.
        handlerCancel = event -> {
            taskLoadObjects.cancel();
            btnLoadObjects.setText("Load database objects");
            btnLoadObjects.setGraphic(Images.RUN.imageView());
            lblLoadingSchema.setText("");
            lblLoadingTable.setText("");
            bar.setVisible(false);

            btnLoadObjects.setOnAction(handlerLoad);
        };

        handlerLoad = event -> {
            populateTree();
            btnLoadObjects.setText("Cancel loading");
            btnLoadObjects.setGraphic(Images.CLOCK.imageView());
            bar.setVisible(true);

            btnLoadObjects.setOnAction(handlerCancel);
        };
    }

    /**
     * Populates the tree by kicking off an {@link ObjectLoaderTask}. The UI is updated with an indeterminate
     * spinner and labels are updates when things are loaded.
     */
    private void populateTree() {
        this.taskLoadObjects = new ObjectLoaderTask(objectTree, connection);

        taskLoadObjects.loadingSchemaPropertyProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                lblLoadingSchema.setText(newValue);
                lblLoadingTable.setText("");
            });
        });

        taskLoadObjects.loadingTablePropertyProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> lblLoadingTable.setText(newValue));
        });

        taskLoadObjects.setOnSucceeded(event -> {
            logger.debug("Task succeeded");
            stackPane.getChildren().clear();
            stackPane.getChildren().add(paneObjects);
            paneObjects.toFront();
        });

        taskLoadObjects.setOnCancelled(event -> {
            logger.debug("Task cancelled");
            btnLoadObjects.setOnAction(handlerLoad);
        });


        taskLoadObjects.setOnFailed(event -> {
            logger.error("Task failed", taskLoadObjects.getException());
        });

        Thread t = new Thread(taskLoadObjects);
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
                setText(item.getName());
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
