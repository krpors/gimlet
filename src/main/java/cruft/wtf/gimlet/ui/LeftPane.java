package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class LeftPane extends BorderPane {

    private AliasList aliasList = new AliasList();
    private QueryTree queryTree = new QueryTree();
    private ProjectPropertiesPane projectPropertiesPane = new ProjectPropertiesPane();

    private TabPane tabPane;

    public LeftPane() {

        createCenter();
        EventDispatcher.getInstance().register(this);
    }


    public void createCenter() {
        Tab tabProjectProps = new Tab("Project", this.projectPropertiesPane);
        tabProjectProps.setGraphic(Images.COG.imageView());
        tabProjectProps.setClosable(false);
        Tab tabAliases = new Tab("Aliases", this.aliasList);
        tabAliases.setGraphic(Images.ACCOUNT_LOGIN.imageView());
        tabAliases.setClosable(false);
        Tab tabQueries = new Tab("Queries", this.queryTree);
        tabQueries.setGraphic(Images.MAGNIFYING_GLASS.imageView());
        tabQueries.setClosable(false);
        this.tabPane = new TabPane(tabProjectProps, tabAliases, tabQueries);
        setCenter(this.tabPane);
    }

    @Subscribe
    public void onFileOpened(final FileOpenedEvent foe) {
        aliasList.setAliases(foe.getGimletProject().aliasesProperty());
        queryTree.setQueryList(foe.getGimletProject().queriesProperty());
    }
}
