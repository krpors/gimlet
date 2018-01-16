package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.event.ConnectEvent;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

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

    /**
     * This property returns whether a tab is currently selected (i.e. open) in the current view.
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

        getTabs().clear();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onFileOpenedEvent(final FileOpenedEvent event) {
        logger.debug("New file opened, closing all tabs");
        closeAllTabs();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onConnectEvent(final ConnectEvent evt) {
        ConnectionTab tab = null;
        try {
            tab = new ConnectionTab(evt.getAlias());
            getTabs().add(tab);
            getSelectionModel().select(tab);
        } catch (SQLException e) {
            logger.error("Could not connect to '{}'", evt.getAlias().getName());
            Utils.showExceptionDialog(
                    String.format("Unable to connect to '%s'", evt.getAlias().getName()),
                    String.format("Failed to connect to '%s'", evt.getAlias().getUrl()),
                    e);
        }
    }
}
