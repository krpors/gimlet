package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.ConnectEvent;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import cruft.wtf.gimlet.event.FileSavedEvent;
import cruft.wtf.gimlet.event.QueryExecutedEvent;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This statusbar is located under the main window and is used for reporting application status to the user.
 */
@SuppressWarnings("unused")
public class StatusBar extends HBox {

    private Label lblStatus;

    private final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss ");

    public StatusBar() {
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(25);
        setPadding(new Insets(2.5, 5, 2.5, 5));

        lblStatus = new Label();
        getChildren().add(lblStatus);

        setStatus("Gimlet loaded!");
        // When everything worked, register ourselves to the EventDispatcher so we get
        // notified of application wide events.
        EventDispatcher.getInstance().register(this);
    }

    public void setStatus(String status, Object... fmt) {
        String date = sdf.format(new Date());
        String s = String.format(status, fmt);
        Platform.runLater(() -> lblStatus.setText(date + s));
    }

    @Subscribe
    public void onConnectEvent(final ConnectEvent event) {
        switch (event.getType()) {
            case CLOSED: // lol
                setStatus("Connection closed to '%s'", event.getAlias().getName());
                break;
            case CONNECTED:
                setStatus("Connected to '%s' (%s)!", event.getAlias().getName(), event.getAlias().getUrl());
                break;
        }
    }

    @Subscribe
    public void onFileSaved(final FileSavedEvent event) {
        setStatus("File saved: %s", event.getFile().getAbsolutePath());
    }

    @Subscribe
    public void onFileOpened(final FileOpenedEvent event) {
        setStatus("Opened '%s' (%s)", event.getGimletProject().getName(), event.getFile().getAbsolutePath());
    }

    @Subscribe
    public void onQueryExecuted(final QueryExecutedEvent event) {
        setStatus("Query executed in %d ms, containing %d rows ", event.getRuntime(), event.getRowCount());
    }
}
