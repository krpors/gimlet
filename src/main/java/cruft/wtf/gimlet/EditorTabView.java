package cruft.wtf.gimlet;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.ConnectEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.sql.SQLException;

public class EditorTabView extends TabPane {

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
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to connect!");
            alert.setHeaderText("Unable to connect to '" + evt.getAlias().getName() + "'");
            alert.showAndWait();
        }
    }
}
