package cruft.wtf.gimlet;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.QueryEditEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class EditorTabView extends TabPane {

    public EditorTabView() {
        EventDispatcher.getInstance().register(this);

        setTabMaxWidth(150);
        Tab staticTab = new Tab("WelcomE!");
        staticTab.setClosable(false);
        getTabs().add(staticTab);
    }

    @Subscribe
    public void onQueryEdit(final QueryEditEvent editEvent) {
        for (Tab tab : getTabs()) {
            if (tab instanceof QueryTab) {
                QueryTab queryTab = (QueryTab) tab;
                if (queryTab.getQuery().getName().equals(editEvent.getQuery().getName())) {
                    getSelectionModel().select(queryTab);
                    return;
                }
            }
        }

        QueryTab tab = new QueryTab(editEvent.getQuery());
        getTabs().add(tab);

        getSelectionModel().select(tab);
    }
}
