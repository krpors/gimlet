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

import java.sql.Connection;

public class ObjectsTab extends Tab {

    private Connection connection;

    private Button btnLoadObjects = new Button("Load database objects", Images.CLOCK.imageView());

    private Label lblLoadingSchema = new Label();

    private Label lblLoadingTable = new Label();

    private ProgressIndicator bar = new ProgressIndicator();

    private TreeView<String> objectTree = new TreeView<>();

    private StackPane stackPane = new StackPane();

    private ObjectLoaderTask taskLoadObjects;

    private EventHandler<ActionEvent> cancel = event -> {
        System.out.println("cancel!");
        taskLoadObjects.cancel();
    };

    private EventHandler<ActionEvent> handler = event -> {
        populateTree();
        btnLoadObjects.setText("Cancel");
        btnLoadObjects.setOnAction(cancel);
    };

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

        btnLoadObjects.setOnAction(handler);

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
            stackPane.getChildren().forEach(node -> node.setVisible(false));
            objectTree.setVisible(true);
        });

        taskLoadObjects.setOnCancelled(event -> {
            lblLoadingSchema.setText("Loading cancelled.");
        });

        Thread t = new Thread(taskLoadObjects);
        t.start();
    }
}
