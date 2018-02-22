package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class ProjectPropertiesPane extends ScrollPane {

    private TextField txtFileName = new TextField();

    private TextField txtName = new TextField();

    private TextArea txtDesc = new TextArea();

    public ProjectPropertiesPane() {
        txtDesc.setPrefRowCount(3);

        FormPane pane = new FormPane();

        txtFileName.setEditable(false);
        pane.add("File: ", txtFileName);
        pane.add("Name: ", txtName);
        pane.add("Description: ", txtDesc);

        setFitToWidth(true);

        setContent(pane);

        EventDispatcher.getInstance().register(this);
    }

    @Subscribe
    public void onFileOpened(final FileOpenedEvent foe) {
        txtFileName.setText(foe.getGimletProject().getFile().getAbsolutePath());
        txtName.textProperty().bindBidirectional(foe.getGimletProject().nameProperty());
        txtDesc.textProperty().bindBidirectional(foe.getGimletProject().descriptionProperty());
    }
}
