package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.Configuration;
import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.conf.GimletProject;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import cruft.wtf.gimlet.event.FileSavedEvent;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;

/**
 * The main Scene of Gimlet.
 */
public class GimletScene extends Scene {

    private static Logger logger = LoggerFactory.getLogger(GimletApp.class);

    /**
     * There is only one EditorTabView throughout Gimlet. So once we created it, we can reference to it statically.
     */
    public static EditorTabView editorTabView;

    /**
     * The list containing the aliases.
     */
    private AliasList aliasList;

    /**
     * A TreeView containing the queries.
     */
    private QueryTree queryConfigurationTree;

    /**
     * The global reference to the opened GimletProject. Can be null if none is opened... Is there a better way?
     */
    public static GimletProject gimletProject;

    public GimletScene(Parent root) {
        super(root);

        BorderPane pane = new BorderPane();

        editorTabView = new EditorTabView();
        SplitPane centerPane = new SplitPane(createLeft(), editorTabView);
        centerPane.setDividerPosition(0, 0.25);

        pane.setTop(createMenuBar());
        pane.setCenter(centerPane);
        pane.setBottom(createBottom());
    }


    /**
     * Load up a project file.
     *
     * @param file The file to (attempt) to open. When it fails, the user is notified.
     */
    public void loadProjectFile(final File file) {
        try {
            GimletApp.gimletProject = GimletProject.read(file);

            Configuration.instance.setProperty(Configuration.Key.LAST_PROJECT_FILE, file.getAbsolutePath());

            aliasList.setAliases(GimletApp.gimletProject.aliasesProperty());
            queryConfigurationTree.setQueryList(GimletApp.gimletProject.queriesProperty());

            // Notify our listeners.
            logger.info("Succesfully read '{}'", file);
            EventDispatcher.getInstance().post(new FileOpenedEvent(file, GimletApp.gimletProject));
        } catch (JAXBException e) {
            logger.error("Unable to unmarshal " + file, e);
            Utils.showError("Invalid Gimlet project file", "The specified file could not be read properly.");
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

}
