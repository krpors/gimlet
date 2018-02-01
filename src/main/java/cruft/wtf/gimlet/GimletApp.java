package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.GimletProject;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import cruft.wtf.gimlet.event.FileSavedEvent;
import cruft.wtf.gimlet.ui.ConnectionTabPane;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.ui.Images;
import cruft.wtf.gimlet.ui.LeftPane;
import cruft.wtf.gimlet.ui.StatusBar;
import cruft.wtf.gimlet.ui.dialog.SettingsDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

/**
 * This is the main class how Gimlet can be run. It also contains some global references to certain user interface
 * object instances which allow us for easier access. Unsure whether this is actually proper UI programming, but
 * it seemed to be one of the easiest solutions.
 */
public class GimletApp extends Application {

    private static Logger logger = LoggerFactory.getLogger(GimletApp.class);

    /**
     * Reference to the primary stage.
     */
    private Stage primaryStage;

    /**
     * There is only one ConnectionTabPane throughout Gimlet. So once we created it, we can reference to it statically.
     */
    public static ConnectionTabPane connectionTabPane;

    private SplitPane centerPane;

    /**
     * The global reference to the opened GimletProject. Can be null if none is opened... Is there a better way?
     */
    private GimletProject gimletProject;

    /**
     * Entry point.
     *
     * @param args Currently unused.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Adds a shutdown hook so when the application is shutdown, some actions will run.
     */
    private void addShutdownHook() {
        logger.debug("Adding shutdown hook.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.debug("Invoking shutdown hook.");
            connectionTabPane.closeAllTabs();

            try {
                Configuration c = Configuration.getInstance();
                c.setProperty(Configuration.Key.WINDOW_MAXIMIZED, primaryStage.isMaximized());
                c.write();
            } catch (IOException e) {
                System.err.println("Unable to write the properties file at JVM exit!");
                e.printStackTrace();
            }
        }));
    }

    /**
     * Read configuration file, and read the last opened project file and reopen it, if present.
     */
    public void initConfigs() {
        try {
            logger.debug("Loading Gimlet configuration");
            Configuration c = Configuration.getInstance();
            c.load();

            Optional<String> lastProject = c.getStringProperty(Configuration.Key.LAST_PROJECT_FILE);
            if (lastProject.isPresent()) {
                logger.info("Loading up most recent project file '{}'", lastProject.get());
                loadProjectFile(new File(lastProject.get()));
            } else {
                // TODO: instead of setting an empty project, display a screen saying CLICK NEW!!!
                this.gimletProject = new GimletProject();
            }
        } catch (IOException e) {
            Utils.showExceptionDialog(
                    "Error!",
                    "Could not load properties file " + Configuration.getInstance().getConfigFile(),
                    e);
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

            Configuration.getInstance().setProperty(Configuration.Key.LAST_PROJECT_FILE, file.getAbsolutePath());

            // Notify our listeners.
            // Notify our listeners.
            logger.info("Succesfully read '{}'", file);

            this.primaryStage.titleProperty().bind(Bindings.concat("Gimlet - ", this.gimletProject.nameProperty()));

            EventDispatcher.getInstance().post(new FileOpenedEvent(file, this.gimletProject));
        } catch (JAXBException e) {
            logger.error("Unable to unmarshal " + file, e);
            Utils.showError("Invalid Gimlet project file", "The specified file could not be read properly.");
        } catch (FileNotFoundException e) {
            logger.error("Could not load Gimlet project file", e);
            Utils.showExceptionDialog("File could not be found", "The file could not be found.", e);
            // TODO: fix that the application won't read aliases (which are null) at this point.
        }
    }

    /**
     * Shows a save-as dialog.
     */
    private void showSaveAsDialog() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gimlet project files", "*.gml"));
        chooser.setTitle("Save Gimlet project as");
        chooser.setInitialFileName(gimletProject.getName() + ".gml");
        File file = chooser.showSaveDialog(null);
        if (file == null) {
            return;
        }

        try {
            gimletProject.writeToFile(file);
            gimletProject.setFile(file); // update the file reference so the editor is now opened in that one.
            FileSavedEvent event = new FileSavedEvent(file);
            EventDispatcher.getInstance().post(event);
        } catch (JAXBException e) {
            e.printStackTrace();
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
        fileItemNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));

        MenuItem fileItemOpen = new MenuItem("Open...");
        fileItemOpen.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileItemOpen.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Gimlet project file");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gimlet project files", "*.gml"));
            File file = chooser.showOpenDialog(null);
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
                logger.info("Writing project file to '{}'", gimletProject.getFile());
                try {
                    gimletProject.writeToFile(gimletProject.getFile());
                    EventDispatcher.getInstance().post(new FileSavedEvent(gimletProject.getFile()));
                } catch (JAXBException e) {
                    logger.error("Failed to write to file '{}'", gimletProject.getFile());
                    Utils.showExceptionDialog("Failed to write to file", "Could not write to file.", e);
                }
            } else {
                // no file to overwrite, so save project as ...
                showSaveAsDialog();
            }
        });

        MenuItem fileItemSaveAs = new MenuItem("Save as...");
        fileItemSaveAs.setOnAction(event -> showSaveAsDialog());

        MenuItem fileItemSettings = new MenuItem("Settings...");
        fileItemSettings.setGraphic(Images.COG.imageView());
        fileItemSettings.setOnAction(event -> {
            SettingsDialog dlg = new SettingsDialog();
            dlg.showAndWait();
        });

        MenuItem fileItemExit = new MenuItem("Exit");
        fileItemExit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        fileItemExit.setOnAction(event -> Platform.exit());
        fileItemExit.setGraphic(Images.ACCOUNT_LOGOUT.imageView());

        menuFile.getItems().add(fileItemNew);
        menuFile.getItems().add(fileItemOpen);
        menuFile.getItems().add(fileItemSave);
        menuFile.getItems().add(fileItemSaveAs);
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(fileItemSettings);
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
        return new LeftPane();
    }

    /**
     * This method creates the bottom part of the application, containing the statusbar and other cruft.
     *
     * @return
     */
    private Node createBottom() {
        BorderPane pane = new BorderPane();

        Accordion accordion = new Accordion();
        TitledPane pane1 = new TitledPane("Messages", new TextArea("Messages published throughout Gimlet"));
        pane1.setAnimated(false);
        TitledPane pane2 = new TitledPane("Scratch", new TextArea("Scratch pad"));
        pane2.setAnimated(false);

        accordion.getPanes().addAll(pane1, pane2);

        pane.setCenter(accordion);
        pane.setBottom(new StatusBar());
        return pane;
    }


    /**
     * Creates the {@link Stage} and all other cruft of the main window.
     *
     * @param primaryStage
     * @throws IOException
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        logger.info("Starting up Gimlet");

        this.primaryStage = primaryStage;

        addShutdownHook();

        BorderPane pane = new BorderPane();

        connectionTabPane = new ConnectionTabPane();
        Node left = createLeft();
        centerPane = new SplitPane(left, connectionTabPane);
        SplitPane.setResizableWithParent(left, false);

        pane.setTop(createMenuBar());
        pane.setCenter(centerPane);
        pane.setBottom(createBottom());

        Scene scene = new Scene(pane);
        scene.getStylesheets().add("/css/style.css");

        primaryStage.setTitle("Gimlet");
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        // After all controls are created, read the configuration file (if any).
        initConfigs();

        // Read some properties from the user configuration file.
        Configuration config = Configuration.getInstance();
        config.getBooleanProperty(Configuration.Key.WINDOW_MAXIMIZED).ifPresent(primaryStage::setMaximized);

        // Show the stage after possibly reading and setting window properties.
        primaryStage.show();

        logger.info("Gimlet started and ready");
    }

}
