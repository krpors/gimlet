package cruft.wtf.gimlet;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.ConnectEvent;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class EditorTabView extends TabPane {

    private Logger logger = LoggerFactory.getLogger(EditorTabView.class);

    public EditorTabView() {
        EventDispatcher.getInstance().register(this);
    }

    public ConnectionTab getOpenTab() {
        return (ConnectionTab) getSelectionModel().getSelectedItem();
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
