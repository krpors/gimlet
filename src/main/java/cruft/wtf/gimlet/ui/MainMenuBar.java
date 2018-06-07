package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.Script;
import cruft.wtf.gimlet.ScriptLoader;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.ScriptExecutedEvent;
import cruft.wtf.gimlet.ui.dialog.AboutWindow;
import cruft.wtf.gimlet.ui.dialog.FileDialogs;
import cruft.wtf.gimlet.ui.dialog.SettingsDialog;
import cruft.wtf.gimlet.util.Xdg;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// TODO: perhaps externalize ActionEvents to their own classes?

/**
 * The main menu bar, displayed at the top of the application.
 */
public class MainMenuBar extends MenuBar {

    private static Logger logger = LoggerFactory.getLogger(MainMenuBar.class);

    private final GimletApp gimletApp;

    private Menu menuScripts;

    public MainMenuBar(final GimletApp gimletApp) {
        this.gimletApp = gimletApp;

        Menu menuFile = new Menu("File");

        MenuItem fileItemNew = new MenuItem("New");
        fileItemNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        fileItemNew.setOnAction(event -> FileDialogs.showNewProjectDialog());

        MenuItem fileItemOpen = new MenuItem("Open...");
        fileItemOpen.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileItemOpen.setOnAction(event -> FileDialogs.showOpenProjectDialog());

        MenuItem fileItemSave = new MenuItem("Save", Images.SAVE.imageView());
        fileItemSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        fileItemSave.setOnAction(event -> gimletApp.saveProject());
        fileItemSave.disableProperty().bind(Bindings.isNull(gimletApp.getGimletProjectProperty()));

        MenuItem fileItemSaveAs = new MenuItem("Save as...");
        fileItemSaveAs.setOnAction(event -> FileDialogs.showSaveAsDialog(gimletApp.getGimletProjectProperty().get()));
        fileItemSaveAs.disableProperty().bind(Bindings.isNull(gimletApp.getGimletProjectProperty()));

        MenuItem fileItemSettings = new MenuItem("Settings...");
        fileItemSettings.setGraphic(Images.COG.imageView());
        fileItemSettings.setOnAction(event -> {
            SettingsDialog dlg = new SettingsDialog();
            dlg.showAndWait();
        });

        MenuItem fileItemExit = new MenuItem("Exit");
        fileItemExit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        fileItemExit.setOnAction(event -> {
            gimletApp.askForClosing().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    gimletApp.exit();
                }
            });
        });
        fileItemExit.setGraphic(Images.ACCOUNT_LOGOUT.imageView());

        menuFile.getItems().add(fileItemNew);
        menuFile.getItems().add(fileItemOpen);
        menuFile.getItems().add(fileItemSave);
        menuFile.getItems().add(fileItemSaveAs);
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(fileItemSettings);
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(fileItemExit);

        menuScripts = new Menu("Scripts");
        loadScripts();

        Menu menuHelp = new Menu("Help");
        MenuItem helpItemAbout = new MenuItem("About");
        helpItemAbout.setOnAction(event -> {
            AboutWindow window = new AboutWindow(gimletApp);
            window.show();
        });
        menuHelp.getItems().add(helpItemAbout);

        getMenus().add(menuFile);
        getMenus().add(menuScripts);
        getMenus().add(menuHelp);
    }

    /**
     * Load the scripts from the filesystem
     */
    private void loadScripts() {
        menuScripts.getItems().clear();

        try {
            Path p = Xdg.getConfigHome().resolve("scripts");
            List<Script> s = ScriptLoader.load(p.toString(), true);
            s.stream()
                    .filter(Script::isValid)
                    .forEach(script -> {
                        // Bind objects in the namespace of the script.
                        script.put("gimletapp", gimletApp);
                        script.put("ctp", GimletApp.connectionTabPane);

                        MenuItem item = new MenuItem(script.getName());
                        item.setOnAction(e -> executeScript(script));
                        menuScripts.getItems().add(item);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        menuScripts.getItems().add(new SeparatorMenuItem());
        MenuItem itemReload = new MenuItem("Reload scripts");
        itemReload.setOnAction(event -> loadScripts());
        menuScripts.getItems().add(itemReload);
    }

    private void executeScript(final Script script) {
        try {
            Object o = script.execute();
            EventDispatcher.getInstance().post(new ScriptExecutedEvent(o));
        } catch (ScriptException e) {
            logger.error("Error while executing script", e);
            Utils.showExceptionDialog("Error in script.", "There was an error while executing the script.", e);
        }
    }
}
