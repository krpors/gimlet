package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.ConnectEvent;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import cruft.wtf.gimlet.jdbc.task.ConnectTask;
import cruft.wtf.gimlet.ui.dialog.PasswordInputDialog;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The {@link ConnectionTabPane} contains the tabs associated with {@link ConnectionTab}s.
 */
public class ConnectionTabPane extends TabPane {

    private Logger logger = LoggerFactory.getLogger(ConnectionTabPane.class);

    private SimpleBooleanProperty tabSelectedProperty = new SimpleBooleanProperty(false);

    public ConnectionTabPane() {
        EventDispatcher.getInstance().register(this);
        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            tabSelectedProperty.set(newValue != null);
        });
        setOnKeyPressed(event -> {
            // Close the active result table tab. TODO: this sort of works. The DrillDownTab sometimes fails.
            if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.F4) {
                ConnectionTab selectedConnection = (ConnectionTab) getSelectionModel().getSelectedItem();
                selectedConnection.closeSelectedResultTable();
            }
        });
    }

    public ConnectionTab getSelected() {
        return (ConnectionTab) getSelectionModel().getSelectedItem();
    }

    /**
     * This property returns whether a tab is currently selected (i.e. open) in the current view. It's used by the
     * {@link QueryTree} to determine whether the "Run Query" is enabled or not.
     *
     * @return A {@link ReadOnlyBooleanProperty} whether a tab is currently selected/open.
     */
    public ReadOnlyBooleanProperty tabSelectedProperty() {
        return tabSelectedProperty;
    }

    /**
     * Closes all tabs in this tabpane. As a result, calls the onCloseRequest handler on each tab so
     * the connection is closed, which is bound to that tab.
     */
    public void closeAllTabs() {
        getTabs().forEach(tab -> {
            EventHandler<Event> handler = tab.getOnCloseRequest();
            if (handler != null) {
                handler.handle(null);
            }
        });

        Platform.runLater(() -> getTabs().clear());
    }

    /**
     * When a new file is opened, we will close all tabs, thus also closing all connections.
     *
     * @param event The event.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onFileOpenedEvent(final FileOpenedEvent event) {
        logger.debug("New file opened, closing all tabs");
        closeAllTabs();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onConnectEvent(final ConnectEvent evt) {
        // Only act on event types which are about to be creating new connections.
        if (evt.getType() != ConnectEvent.Type.INITATED) {
            return;
        }

        ConnectionTab tab = new ConnectionTab(evt.getAlias());

        ConnectTask connectTask = new ConnectTask(evt.getAlias());
        if (evt.getAlias().isAskForPassword()) {
            PasswordInputDialog dlg = new PasswordInputDialog(evt.getAlias().getUser());
            Optional<String> pwd = dlg.showAndWait();
            if (pwd.isPresent()) {
                connectTask.setPassword(pwd.get());
            } else {
                // user pressed cancel.
                return;
            }
        }

        connectTask.setOnScheduled(event -> {
            tab.startTimer();
            getTabs().add(tab);
            getSelectionModel().select(tab);
        });

        connectTask.setOnSucceeded(event -> {
            tab.setConnection(connectTask.getValue());
            // Publish another connect event, except of type 'CONNECTED'.
            EventDispatcher.getInstance().post(new ConnectEvent(ConnectEvent.Type.CONNECTED, evt.getAlias()));
        });

        connectTask.setOnFailed(event -> {
            tab.setThrowable(connectTask.getException());
        });

        Thread t = new Thread(connectTask, "Gimlet connection thread");
        t.setDaemon(true);
        t.start();

    }
}
