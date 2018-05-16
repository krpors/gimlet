package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.ui.dialog.AboutWindow;
import cruft.wtf.gimlet.ui.dialog.FileDialogs;
import cruft.wtf.gimlet.ui.dialog.SettingsDialog;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;

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
