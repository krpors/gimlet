package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.GimletProject;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public class GimletApp extends Application {

    /**
     * Reference to the main window. This should never be null, and is a reference to the top-level window.
     * This reference can be used to keep certain other windows in a modal state.
     */
    public static Window mainWindow;

    private AliasTable aliasTable;

    private QueryTree queryConfigurationTree;

    private GimletProject gimletProject;

    public static void main(String[] args) {
        launch(args);
    }

    public void initConfigs() {
        try {
            this.gimletProject = GimletProject.read(GimletProject.class.getResourceAsStream("/project.xml"));
        } catch (JAXBException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    /**
     * Load up a project file.
     *
     * @param file The file to (attempt) to open. When it fails, the user is notified.
     */
    public void loadProjectFile(final File file) {
        try {
            this.gimletProject = GimletProject.read(file);
            aliasTable.setAliases(this.gimletProject.getAliases());
            queryConfigurationTree.setQueryConfiguration(this.gimletProject.getQueries());

        } catch (JAXBException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please try another one!", ButtonType.OK);
            alert.setHeaderText("That wasn't a proper Gimlet file...");
            alert.showAndWait();
            // TODO: better notification.
        }
    }

    /**
     * Creates the menubar at the top of the application.
     *
     * @return The {@link MenuBar}.
     */
    public MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem fileItemOpen = new MenuItem("Open...");
        fileItemOpen.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileItemOpen.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Gimlet project file");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gimlet project files", "*.xml"));
            File file = chooser.showOpenDialog(GimletApp.mainWindow);
            if (file == null) {
                // user pressed cancel.
                return;
            }

            loadProjectFile(file);
            // TODO: invalidate UI, close tabs, connections, etc etc.
        });

        MenuItem fileItemSave = new MenuItem("Save");
        fileItemSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        fileItemSave.setOnAction(event -> {
            if (gimletProject != null && gimletProject.getFile() != null) {
                // Save in place.
                System.out.println("Saving in place!");
            }
        });

        MenuItem fileItemExit = new MenuItem("Exit");
        fileItemExit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        fileItemExit.setOnAction(event -> Platform.exit());

        menuFile.getItems().add(fileItemOpen);
        menuFile.getItems().add(fileItemSave);
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(fileItemExit);

        Menu menuHelp = new Menu("Help");

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuHelp);
        return menuBar;
    }

    /**
     * Creates the left-hand side of the application space. This contains the parts to configure queries and aliases.
     *
     * @return The {@link Node} containing the left portion of the application.
     */
    private Node createLeft() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        aliasTable = new AliasTable();
        aliasTable.setAliases(gimletProject.getAliases());

        queryConfigurationTree = new QueryTree();
        queryConfigurationTree.setQueryConfiguration(gimletProject.getQueries());

        tabPane.getTabs().add(new Tab("Aliases", aliasTable));
        tabPane.getTabs().add(new Tab("Queries", queryConfigurationTree));

        return tabPane;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        initConfigs();

        BorderPane pane = new BorderPane();

        SplitPane centerPane = new SplitPane(createLeft(), new EditorTabView());
        centerPane.setDividerPosition(0, 0.25);

        pane.setTop(createMenuBar());
        pane.setCenter(centerPane);

        Scene scene = new Scene(pane);

        // We started, set our main window reference!
        mainWindow = primaryStage.getOwner();

        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setTitle("Gimlet");
        primaryStage.show();
    }

}
