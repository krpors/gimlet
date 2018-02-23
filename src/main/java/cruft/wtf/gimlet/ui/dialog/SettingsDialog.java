package cruft.wtf.gimlet.ui.dialog;


import cruft.wtf.gimlet.Configuration;
import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.ui.FormPane;
import cruft.wtf.gimlet.ui.Images;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Dialog holding application settings.
 */
public class SettingsDialog extends Dialog<String> {

    private static Logger logger = LoggerFactory.getLogger(SettingsDialog.class);

    private TextField txtTruncateSize;

    public SettingsDialog() {
        initOwner(GimletApp.window);
        setTitle("Settings");
        setHeaderText("Gimlet settings");
        setGraphic(Images.PERSON.imageView());

        FormPane pane = new FormPane();

        Configuration c = Configuration.getInstance();

        txtTruncateSize = new TextField(c.getProperty(Configuration.Key.TRUNCATE_SIZE));
        txtTruncateSize.setTextFormatter(Utils.createFormatter());
        txtTruncateSize.setTooltip(new Tooltip("The amount of characters to display in columns until they are truncated."));

        pane.add("Column truncate length", txtTruncateSize);

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
        Configuration c = Configuration.getInstance();
        try {
            c.setProperty(Configuration.Key.TRUNCATE_SIZE, txtTruncateSize.getText());
            c.write();
        } catch (IOException e) {
            logger.error("Unable to write configuration file", e);
            Utils.showExceptionDialog("Unable to write properties", "CAN'T!", e);
        }
    }
}
