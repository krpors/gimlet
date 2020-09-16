package cruft.wtf.gimlet.ui.dialog;


import cruft.wtf.gimlet.Configuration;
import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.ui.FormPane;
import cruft.wtf.gimlet.ui.Images;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Dialog holding application settings.
 */
public class SettingsDialog extends Dialog<String> {

    private static Logger logger = LoggerFactory.getLogger(SettingsDialog.class);

    private TextField txtTruncateSize;

    private CheckBox chkSaveOnExit;

    private CheckBox chkConfirmAppExit;

    private CheckBox chkEnableDarkTheme;

    public SettingsDialog() {
        initOwner(GimletApp.window);
        setTitle("Settings");
        setHeaderText("Gimlet settings");
        setGraphic(Images.PERSON.imageView());

        FormPane pane = new FormPane();

        txtTruncateSize = new TextField(Configuration.getStringProperty(Configuration.Key.TRUNCATE_SIZE).orElse("32"));
        txtTruncateSize.setTextFormatter(Utils.createFormatter());
        txtTruncateSize.setTooltip(new Tooltip("The amount of characters to display in columns until they are truncated."));
        pane.add("Column truncate length", txtTruncateSize);

        chkSaveOnExit = new CheckBox();
        chkSaveOnExit.setSelected(Configuration.getBooleanProperty(Configuration.Key.SAVE_ON_EXIT).orElse(false));
        pane.add("Save project automatically on exit", chkSaveOnExit);

        chkConfirmAppExit = new CheckBox();
        chkConfirmAppExit.setSelected(Configuration.getBooleanProperty(Configuration.Key.CONFIRM_APPLICATION_EXIT).orElse(true));
        chkConfirmAppExit.setTooltip(new Tooltip("Check this box to confirm to exit the application"));
        pane.add("Confirm application exit", chkConfirmAppExit);

        chkEnableDarkTheme = new CheckBox();
        chkEnableDarkTheme.setSelected(Configuration.getBooleanProperty(Configuration.Key.ENABLE_DARK_THEME).orElse(false));
        chkEnableDarkTheme.setTooltip(new Tooltip("Enables the dark theme for the application"));
        chkEnableDarkTheme.setOnAction(actionEvent -> {
            if (chkEnableDarkTheme.isSelected()) {
                Utils.showInfo("Dark theme is experimental", "The dark theme is experimental. Restart the application to fully enable it.");
            }
        });
        pane.add("Enable dark theme (experimental)", chkEnableDarkTheme);

        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);

        setResultConverter(param -> {
            if (param == ButtonType.OK) {
                apply();
            }
            return null;
        });

    }

    private void apply() {
        try {
            Configuration.setProperty(Configuration.Key.TRUNCATE_SIZE, txtTruncateSize.getText());
            Configuration.setProperty(Configuration.Key.SAVE_ON_EXIT, chkSaveOnExit.isSelected());
            Configuration.setProperty(Configuration.Key.CONFIRM_APPLICATION_EXIT, chkConfirmAppExit.isSelected());
            Configuration.setProperty(Configuration.Key.ENABLE_DARK_THEME, chkEnableDarkTheme.isSelected());
            Configuration.write();
        } catch (IOException e) {
            logger.error("Unable to write configuration file", e);
            Utils.showExceptionDialog(e, "Unable to write properties", "CAN'T!");
        }
    }
}
