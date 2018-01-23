package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.jdbc.ObjectLoaderTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class ObjectsTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ObjectsTab.class);

    private Connection connection;

    private Button btnLoadObjects = new Button("Load database objects", Images.CLOCK.imageView());

    private Label lblLoadingSchema = new Label();

    private Label lblLoadingTable = new Label();

    private ProgressIndicator bar = new ProgressIndicator();

    private TreeView<String> objectTree = new TreeView<>();

    private StackPane stackPane = new StackPane();

    private ObjectLoaderTask taskLoadObjects;

    private EventHandler<ActionEvent> handlerCancel;

    private EventHandler<ActionEvent> handlerLoad;

    public ObjectsTab() {
        setText("Database Objects");
        setGraphic(Images.DOCUMENT.imageView());
        setClosable(false);

        objectTree.setVisible(false);

        VBox box = new VBox(
                btnLoadObjects,
                bar,
                lblLoadingSchema,
                lblLoadingTable);
        box.setSpacing(10.0);
        box.setAlignment(Pos.CENTER);
        bar.setVisible(false);

        // Initialize some actions for swapping loading/cancelling.
        handlerCancel = event -> {
            taskLoadObjects.cancel();
            btnLoadObjects.setText("Load database objects");
            btnLoadObjects.setOnAction(handlerLoad);
            lblLoadingSchema.setText("");
            lblLoadingTable.setText("");
        };

        handlerLoad = event -> {
            populateTree();
            btnLoadObjects.setText("Cancel loading");
            btnLoadObjects.setOnAction(handlerCancel);
        };

        btnLoadObjects.setOnAction(handlerLoad);

        stackPane.getChildren().add(box);
        stackPane.getChildren().add(objectTree);

        setContent(stackPane);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

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
            stackPane.getChildren().forEach(node -> node.setVisible(false));
            objectTree.setVisible(true);
        });

        taskLoadObjects.setOnCancelled(event -> {
            logger.debug("Task cancelled");
            btnLoadObjects.setOnAction(handlerLoad);
        });


        taskLoadObjects.setOnFailed(event -> {
            logger.error("Task failed", taskLoadObjects.getException());
        });

        Thread t = new Thread(taskLoadObjects);
        t.start();
    }
}
