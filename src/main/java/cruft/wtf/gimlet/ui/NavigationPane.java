package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.event.EventDispatcher;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class NavigationPane extends BorderPane {

    private AliasPane aliasPane = new AliasPane();
    private QueryPane queryPane = new QueryPane();

    public NavigationPane() {
        createCenter();

        EventDispatcher.getInstance().register(this);
    }

    public void createCenter() {
        Tab tabAliases = new Tab("Aliases", this.aliasPane);
        tabAliases.setGraphic(Images.ACCOUNT_LOGIN.imageView());
        tabAliases.setClosable(false);
        Tab tabQueries = new Tab("Queries", this.queryPane);
        tabQueries.setGraphic(Images.MAGNIFYING_GLASS.imageView());
        tabQueries.setClosable(false);
        TabPane tabPane = new TabPane(tabAliases, tabQueries);
        setCenter(tabPane);
    }
}
