package cruft.wtf.gimlet;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.FileSavedEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusBar extends HBox {

    private Label lblStatus;

    public StatusBar() {
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(25);
        setPadding(new Insets(2.5, 5, 2.5, 5));

        lblStatus = new Label("Gimlet loaded!");
        getChildren().add(lblStatus);

        // When everything worked, register ourselves to the EventDispatcher so we get
        // notified of application wide events.
        EventDispatcher.getInstance().register(this);
    }

    @Subscribe
    public void onFileSaved(final FileSavedEvent event) {
        lblStatus.setText("File saved: " + event.getFile().getAbsolutePath());
    }
}
