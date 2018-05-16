package cruft.wtf.gimlet;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.conf.GimletProject;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import cruft.wtf.gimlet.event.FileSavedEvent;
import cruft.wtf.gimlet.event.LoadProjectEvent;
import cruft.wtf.gimlet.ui.ConnectionTabPane;
import cruft.wtf.gimlet.ui.Images;
import cruft.wtf.gimlet.ui.LogTable;
import cruft.wtf.gimlet.ui.MainMenuBar;
import cruft.wtf.gimlet.ui.NavigationPane;
import cruft.wtf.gimlet.ui.ProjectPropertiesPane;
import cruft.wtf.gimlet.ui.StatusBar;
import cruft.wtf.gimlet.util.VersionInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.krb5.Config;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

// TODO: this class is getting too big I suppose. Needs refactoring?

/**
 * This is the main class how Gimlet can be run. It also contains some global references to certain user interface
 * object instances which allow us for easier access. Unsure whether this is actually proper UI programming, but
 * it seemed to be one of the easiest solutions.
 */
public class GimletApp extends Application {

    private static Logger logger = LoggerFactory.getLogger(GimletApp.class);

    /**
     * This JavaFX property contains the {@link GimletProject} property. Based on the
     * value (whether it is null or not, etc.) certain UI elements are rendered disabled
     * or not. See also the {@link MainMenuBar}.
     */
    private ObjectProperty<GimletProject> gimletProjectObjectProperty = new SimpleObjectProperty<>();

    /**
     * Reference to the primary stage.
     */
    private Stage primaryStage;

    /**
     * There is only one ConnectionTabPane throughout Gimlet. So once we created it, we can reference to it statically.
     */
    public static ConnectionTabPane connectionTabPane;

    /**
     * This window is used to provide as a parent for multiple dialogs.
     */
    public static Window window;

    private Parent mainContentPane;

    private Label lblOpenOrNewProject;

    /**
     * Entry point.
     *
     * @param args Currently unused.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Constructor.
     */
    public GimletApp() {
        // Register this class with the event dispatcher.
        EventDispatcher.getInstance().register(this);
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
            } catch (Exception e) {
                System.err.println("Unable to write the properties file at JVM exit!");
                e.printStackTrace();
            }
        }));
    }

    /**
     * Read configuration file, and read the last opened project file and reopen it, if present.
     */
    private void initConfigs() {
        try {
            logger.debug("Loading Gimlet configuration");
            Configuration c = Configuration.getInstance();
            c.load();

            Optional<String> lastProject = c.getStringProperty(Configuration.Key.LAST_PROJECT_FILE);
            if (lastProject.isPresent()) {
                logger.info("Loading up most recent project file '{}'", lastProject.get());
                loadProjectFile(new File(lastProject.get()));
            }
        } catch (IOException e) {
            Utils.showExceptionDialog(
                    "Error!",
                    "Could not load properties file " + Configuration.getInstance().getConfigFile(),
                    e);
        }
    }

    public ObjectProperty<GimletProject> getGimletProjectProperty() {
        return gimletProjectObjectProperty;
    }

    /**
     * Load up a project file.
     *
     * @param file The file to (attempt) to open. When it fails, the user is notified.
     */
    public void loadProjectFile(final File file) {
        Configuration config = Configuration.getInstance();

        try {
            this.gimletProjectObjectProperty.setValue(GimletProject.read(file));
            GimletProject gimletProject = gimletProjectObjectProperty.get();
            logger.info("Successfully read '{}'", file);

            config.setProperty(Configuration.Key.LAST_PROJECT_FILE, file.getAbsolutePath());

            this.primaryStage.titleProperty().bind(Bindings.concat(VersionInfo.getVersionString() + " - ", gimletProject.nameProperty()));

            EventDispatcher.getInstance().post(new FileOpenedEvent(file, gimletProject));

            // Hide the informative label to open a new project etc, and show the main content pane instead.
            lblOpenOrNewProject.setVisible(false);
            mainContentPane.setVisible(true);
        } catch (JAXBException e) {
            logger.error("Unable to unmarshal " + file, e);
            Utils.showError("Invalid Gimlet project file", "The specified file could not be read properly.");
        } catch (FileNotFoundException e) {
            logger.error("Could not load Gimlet project file", e);
            Utils.showExceptionDialog("File could not be found", "The file could not be found.", e);
            config.remove(Configuration.Key.LAST_PROJECT_FILE);
        }
    }

    /**
     * Saves the project in-place.
     */
    public void saveProject() {
        GimletProject gimletProject = gimletProjectObjectProperty.get();

        // Sanity checks to prevent null pointers.
        if (gimletProject != null && gimletProject.getFile() != null) {
            logger.info("Writing project file to '{}'", gimletProject.getFile());
            try {
                gimletProject.writeToFile();
                EventDispatcher.getInstance().post(new FileSavedEvent(gimletProject.getFile()));
            } catch (JAXBException e) {
                logger.error("Failed to write to file '{}'", gimletProject.getFile());
                Utils.showExceptionDialog("Failed to write to file", "Could not write to file.", e);
            }
        } else {
            logger.warn("gimletProject is null or file property is null?");
            Utils.showExceptionDialog("Project property is null", "Cannot save project property!?", new Exception("Fix me!"));
        }
    }

    /**
     * Creates the left-hand side of the application space. This contains the parts to configure queries and aliases.
     *
     * @return The {@link Node} containing the left portion of the application.
     */
    private Node createLeft() {
        NavigationPane left = new NavigationPane();
        left.setPrefWidth(320);
        return left;
    }

    /**
     * This method creates the bottom part of the application, containing the statusbar and other cruft.
     *
     * @return
     */
    private Node createBottom() {
        BorderPane pane = new BorderPane();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setSide(Side.BOTTOM);

        Tab tabProps = new Tab("Project properties", new ProjectPropertiesPane());
        tabProps.setGraphic(Images.COG.imageView());

        Tab tabLog = new Tab("Logging", new LogTable());
        tabLog.setGraphic(Images.MAGNIFYING_GLASS.imageView());

        tabPane.getTabs().add(tabProps);
        tabPane.getTabs().add(tabLog);

        pane.setCenter(tabPane);
        pane.setBottom(new StatusBar());

        return pane;
    }

    /**
     * Pops up a dialog asking to exit or not.
     *
     * @return The result of the confirmation dialog.
     */
    public Optional<ButtonType> askForClosing() {
        Configuration c = Configuration.getInstance();

        // Check if we are supposed to ask the user to exit the application.
        if (c.getBooleanProperty(Configuration.Key.CONFIRM_APPLICATION_EXIT).orElse(true)) {
            return Utils.showConfirm(
                    "Are you sure you want to exit?",
                    "Close Gimlet",
                    "This will discard any unsaved changes.");
        }

        // In this case, don't ask the user to exit, just assume OK.
        return Optional.of(ButtonType.OK);
    }

    /**
     * Exits the application properly.
     */
    public void exit() {
        // TODO: also!! The configuration API is ugly as hell!
        Configuration c = Configuration.getInstance();
        c.getBooleanProperty(Configuration.Key.SAVE_ON_EXIT).ifPresent(aBoolean -> {
            GimletProject gimletProject = gimletProjectObjectProperty.get();
            if (aBoolean && gimletProject != null && gimletProject.getFile() != null) {
                try {
                    gimletProject.writeToFile();
                    logger.info("Written to file {} at exit", gimletProject.getFile());
                } catch (JAXBException e) {
                    logger.error("Unable to save file", e);
                    Utils.showExceptionDialog("Unable to save", "Argh", e);
                }
            }
        });

        Platform.exit();
    }

    private Parent createNavigationCenterBottom() {
        connectionTabPane = new ConnectionTabPane();

        Node bottom = createBottom();
        SplitPane.setResizableWithParent(bottom, false);

        BorderPane paneNavigationConnections = new BorderPane();
        Node left = createLeft();
        paneNavigationConnections.setLeft(left);
        paneNavigationConnections.setCenter(connectionTabPane);

        // The splitpane, containing the upper borderpane (alias/query + connections)
        // and the bottom part, containing logging and project properties.
        SplitPane splitPane = new SplitPane(paneNavigationConnections, bottom);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.6);

        return splitPane;
    }

    /**
     * This method creates the main content: the menu bar itself and the rest of the application.
     *
     * @return The BorderPane containing the menu bar and the actual program contents.
     */
    private Parent createMainContent() {
        mainContentPane = createNavigationCenterBottom();

        // The pane containing menu bar, and the splitpanes.
        BorderPane pane = new BorderPane();

        pane.setTop(new MainMenuBar(this));
        pane.setCenter(mainContentPane);

        lblOpenOrNewProject = new Label("Create a new project or open an existing one.");
        lblOpenOrNewProject.setStyle("-fx-font-size: 25px");
        lblOpenOrNewProject.setVisible(false);

        // Main content pane invisible, label visible. This will change depending on whether
        // we have opened a project or not.
        mainContentPane.setVisible(false);
        lblOpenOrNewProject.setVisible(true);

        StackPane parentContentStackPane = new StackPane();
        parentContentStackPane.getChildren().add(pane);
        parentContentStackPane.getChildren().add(lblOpenOrNewProject);
        return parentContentStackPane;
    }

    /**
     * Subscribes ourselves to the {@link LoadProjectEvent}, which can be emitted from menu's etc.
     *
     * @param event The event, emitted when a file has been selected for opening.
     */
    @Subscribe
    private void onLoadProject(final LoadProjectEvent event) {
        logger.debug("onLoad...");
        loadProjectFile(event.getFile());
    }

    /**
     * Creates the {@link Stage} and all other cruft of the main window.
     *
     * @param primaryStage
     * @throws IOException
     */
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting up Gimlet");

        this.primaryStage = primaryStage;

        addShutdownHook();

        Scene scene = new Scene(createMainContent());
        scene.getStylesheets().add("/css/style.css");

        primaryStage.setTitle(VersionInfo.getVersionString());
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

        window = scene.getWindow();

        scene.getWindow().setOnCloseRequest(event -> {
            askForClosing().ifPresent(buttonType -> {
                if (buttonType != ButtonType.OK) {
                    event.consume();
                    return;
                }

                exit();
            });
        });

        logger.info("{} started and ready", VersionInfo.getVersionString());
    }
}
