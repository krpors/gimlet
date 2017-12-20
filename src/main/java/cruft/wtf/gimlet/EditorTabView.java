package cruft.wtf.gimlet;

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
}
