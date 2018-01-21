package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

public class LeftPane extends BorderPane {

    private AliasList aliasList = new AliasList();
    private QueryTree queryTree = new QueryTree();

    private TextField txtName = new TextField();
    private TextField txtDescription = new TextField();

    private TabPane tabPane;

    public LeftPane() {

        createTop();
        createCenter();
        EventDispatcher.getInstance().register(this);
    }

    public void createTop() {

        FormPane formPane = new FormPane();
        formPane.add("Name:", txtName);
        formPane.add("Description:", txtDescription);

        TitledPane titledPane = new TitledPane("Project properties", formPane);
        titledPane.setAnimated(false);
        titledPane.setCollapsible(false);
        setTop(titledPane);
    }

    public void createCenter() {
        Tab tabAliases = new Tab("Aliases", this.aliasList);
        tabAliases.setGraphic(Images.BOLT.imageView());
        Tab tabQueries = new Tab("Queries", this.queryTree);
        tabQueries.setGraphic(Images.MAGNIFYING_GLASS.imageView());
        this.tabPane = new TabPane(tabAliases, tabQueries);
        setCenter(this.tabPane);
    }

    @Subscribe
    public void onFileOpened(final FileOpenedEvent foe) {
        aliasList.setAliases(foe.getGimletProject().aliasesProperty());
        queryTree.setQueryList(foe.getGimletProject().queriesProperty());
        txtName.textProperty().bindBidirectional(foe.getGimletProject().nameProperty());
        txtDescription.textProperty().bindBidirectional(foe.getGimletProject().descriptionProperty());
    }
}
