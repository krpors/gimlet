package cruft.wtf.gimlet;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.ConnectEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class EditorTabView extends TabPane {

    private Logger logger = LoggerFactory.getLogger(EditorTabView.class);

    public EditorTabView() {
        EventDispatcher.getInstance().register(this);

        setTabMaxWidth(150);
        Tab staticTab = new Tab("Gimlet v1.0.0");
        staticTab.setClosable(false);
        getTabs().add(staticTab);
    }

    @Subscribe
    public void onConnectEvent(ConnectEvent evt) {
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
