package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class ProjectPropertiesPane extends FormPane {
    private TextField txtName = new TextField();
    private TextArea txtDesc = new TextArea();
    private Label lblQueryCount = new Label();

    public ProjectPropertiesPane() {
        txtDesc.setPrefRowCount(3);

        add("Name: ", txtName);
        add("Description: ", txtDesc);
        add("Queries: ", lblQueryCount);

        EventDispatcher.getInstance().register(this);
    }

    @Subscribe
    public void onFileOpened(final FileOpenedEvent foe) {
        txtName.textProperty().bindBidirectional(foe.getGimletProject().nameProperty());
        txtDesc.textProperty().bindBidirectional(foe.getGimletProject().descriptionProperty());
    }
}
