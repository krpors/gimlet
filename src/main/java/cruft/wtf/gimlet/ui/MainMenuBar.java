package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.AboutWindow;
import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.ui.dialog.SettingsDialog;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;

import java.io.File;

// TODO: perhaps externalize ActionEvents to their own classes?

/**
 * The main menu bar, displayed at the top of the application.
 */
public class MainMenuBar extends MenuBar {

    private final GimletApp gimletApp;

    public MainMenuBar(final GimletApp gimletApp) {
        this.gimletApp = gimletApp;

        Menu menuFile = new Menu("File");

        MenuItem fileItemNew = new MenuItem("New");
        fileItemNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        fileItemNew.setOnAction(event -> gimletApp.newProjectFile());

        MenuItem fileItemOpen = new MenuItem("Open...");
        fileItemOpen.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileItemOpen.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Gimlet project file");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gimlet project files", "*.gml"));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
            File file = chooser.showOpenDialog(GimletApp.window);
            if (file == null) {
                // user pressed cancel.
                return;
            }

            gimletApp.loadProjectFile(file);
        });

        MenuItem fileItemSave = new MenuItem("Save", Images.SAVE.imageView());
        fileItemSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        fileItemSave.setOnAction(event -> gimletApp.saveProject());
        fileItemSave.disableProperty().bind(Bindings.isNull(gimletApp.getGimletProjectProperty()));

        MenuItem fileItemSaveAs = new MenuItem("Save as...");
        fileItemSaveAs.setOnAction(event -> gimletApp.showSaveAsDialog());
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

        Menu menuHelp = new Menu("Help");
        MenuItem helpItemAbout = new MenuItem("About");
        helpItemAbout.setOnAction(event -> {
            AboutWindow window = new AboutWindow(gimletApp);
            window.show();
        });
        menuHelp.getItems().add(helpItemAbout);

        getMenus().add(menuFile);
        getMenus().add(menuHelp);
    }
}
