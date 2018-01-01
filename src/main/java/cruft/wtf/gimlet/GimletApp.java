package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.GimletProject;
import cruft.wtf.gimlet.event.ConnectEvent;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import cruft.wtf.gimlet.event.FileSavedEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GimletApp extends Application {

    private static Logger logger = LoggerFactory.getLogger(GimletApp.class);

    /**
     * Reference to the main window. This should never be null, and is a reference to the top-level window.
     * This reference can be used to keep certain other windows in a modal state.
     */
    public static Window mainWindow;

    private AliasList aliasList;

    private QueryTree queryConfigurationTree;

    /**
     * The global reference to the opened GimletProject. Can be null if none is opened... Is there a better way?
     */
    public static GimletProject gimletProject;

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
            aliasList.setAliases(this.gimletProject.aliasesProperty());
            queryConfigurationTree.setQueryList(this.gimletProject.queriesProperty());

            // Notify our listeners.
            logger.info("Succesfully read '{}'", file);
            EventDispatcher.getInstance().post(new FileOpenedEvent(file, this.gimletProject));
        } catch (JAXBException e) {
            logger.error("Unable to unmarshal " + file, e);
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

        MenuItem fileItemNew = new MenuItem("New");
        fileItemNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));;

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
                try {
                    gimletProject.writeToFile(gimletProject.getFile());
                    EventDispatcher.getInstance().post(new FileSavedEvent(gimletProject.getFile()));
                } catch (JAXBException e) {
                    System.out.println("Writing fail0red");
                }
            } else {
                // TODO: pop up save-as?
            }
        });

        MenuItem fileItemExit = new MenuItem("Exit");
        fileItemExit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        fileItemExit.setOnAction(event -> Platform.exit());
        fileItemExit.setGraphic(Images.ACCOUNT_LOGOUT.imageView());

        menuFile.getItems().add(fileItemNew);
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

        aliasList = new AliasList();
        aliasList.setAliases(gimletProject.aliasesProperty());

        queryConfigurationTree = new QueryTree();
        queryConfigurationTree.setQueryList(gimletProject.queriesProperty());

        Tab tabAlias = new Tab("Aliases", aliasList);
        tabAlias.setGraphic(Images.BOLT.imageView());
        tabPane.getTabs().add(tabAlias);

        Tab tabQueries = new Tab("Queries", queryConfigurationTree);
        tabQueries.setGraphic(Images.DASHBOARD.imageView());
        tabPane.getTabs().add(tabQueries);

        return tabPane;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        logger.info("Starting up the Gimlet");

        initConfigs();

        BorderPane pane = new BorderPane();

        SplitPane centerPane = new SplitPane(createLeft(), new EditorTabView());
        centerPane.setDividerPosition(0, 0.25);


        pane.setTop(createMenuBar());
        pane.setCenter(centerPane);
        pane.setBottom(new StatusBar());

        Scene scene = new Scene(pane);

        // We started, set our main window reference!
        mainWindow = primaryStage.getOwner();

        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setTitle("Gimlet");
        primaryStage.show();

        ConnectEvent event = new ConnectEvent(gimletProject.getAliases().get(0));
        EventDispatcher.getInstance().post(event);

        logger.info("Gimlet started");
    }

}
