package cruft.wtf.gimlet.ui.drilldown;


import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.ui.ConnectionTab;
import cruft.wtf.gimlet.ui.Images;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class is a tab where drilldown functionality exists. This tab contains the results
 * of the drilldown functionalities, which are represented as tabs as well.
 *
 * @see DrillDownExecutionTab
 */
public class DrillDownTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(DrillDownTab.class);

    /**
     * Parent {@link ConnectionTab}.
     */
    private final ConnectionTab connectionTab;

    private TabPane tabPaneResultSets;

    private Node emptyPane;

    /**
     * Creates the {@link DrillDownTab} with the given {@link ConnectionTab} parent.
     *
     * @param connectionTab The tab.
     */
    public DrillDownTab(final ConnectionTab connectionTab) {
        this.connectionTab = connectionTab;

        setText("Drill down");
        setClosable(false);
        setGraphic(Images.MAGNIFYING_GLASS.imageView());

        StackPane contentPane = new StackPane();
        emptyPane = createEmptyPane();

        tabPaneResultSets = new TabPane();
        tabPaneResultSets.setVisible(false);

        contentPane.getChildren().add(emptyPane);
        contentPane.getChildren().add(tabPaneResultSets);

        setContent(contentPane);

        tabPaneResultSets.getTabs().addListener((ListChangeListener<Tab>) c -> {
            boolean noTabs = c.getList().size() <= 0;
            emptyPane.setVisible(noTabs);
            tabPaneResultSets.setVisible(!noTabs);
        });
    }

    private Node createEmptyPane() {
        BorderPane pane = new BorderPane();
        pane.setCenter(new Label("Select a query on the left side."));
        return pane;
    }

    public void executeQuery(final Query query, final Map<String, Object> columnMap) {
        DrillDownExecutionTab tab = new DrillDownExecutionTab(this, query, columnMap);
        tabPaneResultSets.getTabs().add(tab);
        tabPaneResultSets.getSelectionModel().select(tab);
        tab.executeQuery();
    }

    /**
     * Closes the tab which is currently selected by a workaround (TabPaneSkin and stuff).
     */
    public void closeSelectedResultTable() {
        // TODO: Extend TabPane with this behaviour, and use that one as the TabPaneResultSets?
        Tab selected = tabPaneResultSets.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TabPaneBehavior b = ((TabPaneSkin) (tabPaneResultSets.getSkin())).getBehavior();
            b.closeTab(selected);
        }
    }

    /**
     * Gets the connection tab this drilldown tab is a child of.
     *
     * @return The connection tab.
     */
    public ConnectionTab getConnectionTab() {
        return connectionTab;
    }
}
