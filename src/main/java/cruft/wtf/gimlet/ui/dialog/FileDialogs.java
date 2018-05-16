package cruft.wtf.gimlet.ui.dialog;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.conf.GimletProject;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileSavedEvent;
import cruft.wtf.gimlet.event.LoadProjectEvent;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;

/**
 * This class provided static functions for displaying simple dialogs for opening, saving or creating a new project file.
 */
public final class FileDialogs {

    private static Logger logger = LoggerFactory.getLogger(FileDialogs.class);

    private static final FileChooser.ExtensionFilter filterGml = new FileChooser.ExtensionFilter("Gimlet project files", "*.gml");
    private static final FileChooser.ExtensionFilter filterAll = new FileChooser.ExtensionFilter("All files", "*.*");

    /**
     * Shows a dialog to open a project file.
     */
    public static void showOpenProjectDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Gimlet project file");
        chooser.getExtensionFilters().addAll(filterGml, filterAll);
        File file = chooser.showOpenDialog(GimletApp.window);
        if (file == null) {
            // user pressed cancel.
            return;
        }

        EventDispatcher.getInstance().post(new LoadProjectEvent(file));
    }

    /**
     * Creates a new project file, by showing a dialog first.
     */
    public static void showNewProjectDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select location for the new Gimlet project");
        chooser.getExtensionFilters().add(filterGml);
        File file = chooser.showSaveDialog(GimletApp.window);
        if (file == null) {
            // user pressed cancel.
            return;
        }
        // TODO: add .gml to file if not explicitly given.
        GimletProject temp = new GimletProject();
        temp.setFile(file);
        try {
            temp.writeToFile();
            EventDispatcher.getInstance().post(new LoadProjectEvent(file));
        } catch (JAXBException e) {
            logger.error("Unable to write project to file", e);
        }
    }


    /**
     * Shows a save-as dialog.
     */
    public static void showSaveAsDialog(final GimletProject gimletProject) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(filterGml);
        chooser.setTitle("Save Gimlet project as");
        chooser.setInitialFileName(gimletProject.getName() + ".gml");
        File file = chooser.showSaveDialog(null);
        if (file == null) {
            return;
        }

        try {
            gimletProject.setFile(file); // update the file reference so the editor is now opened in that one.
            gimletProject.writeToFile();

            FileSavedEvent event = new FileSavedEvent(file);
            EventDispatcher.getInstance().post(event);

            // Initiate the loading sequence.
            EventDispatcher.getInstance().post(new LoadProjectEvent(file));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
