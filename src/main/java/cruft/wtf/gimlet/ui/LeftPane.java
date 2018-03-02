package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class LeftPane extends BorderPane {

    private AliasPane aliasPane = new AliasPane();
    private QueryTree queryTree = new QueryTree();

    public LeftPane() {
        createCenter();

        EventDispatcher.getInstance().register(this);
    }

    public void createCenter() {
        Tab tabAliases = new Tab("Aliases", this.aliasPane);
        tabAliases.setGraphic(Images.ACCOUNT_LOGIN.imageView());
        tabAliases.setClosable(false);
        Tab tabQueries = new Tab("Queries", this.queryTree);
        tabQueries.setGraphic(Images.MAGNIFYING_GLASS.imageView());
        tabQueries.setClosable(false);
        TabPane tabPane = new TabPane(tabAliases, tabQueries);
        setCenter(tabPane);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onFileOpened(final FileOpenedEvent foe) {
        queryTree.setQueryList(foe.getGimletProject().queriesProperty());
    }
}
