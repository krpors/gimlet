package cruft.wtf.gimlet;


import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.jdbc.NamedParameterPreparedStatement;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is a tab where drilldown functionality exists.
 */
public class DrillDownTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(DrillDownTab.class);

    /**
     * Parent {@link ConnectionTab}.
     */
    private final ConnectionTab connectionTab;

    /**
     * The SQL connection. Delegate from the connection tab.
     */
    private Connection connection;

    private TabPane tabPaneResultSets = new TabPane();

    private Node emptyPane;

    /**
     * Creates the {@link DrillDownTab} with the given {@link ConnectionTab} parent.
     *
     * @param connectionTab The tab.
     */
    public DrillDownTab(final ConnectionTab connectionTab) {
        this.connectionTab = connectionTab;
        this.connection = connectionTab.getConnection();

        setText("Drill down");
        setClosable(false);
        setGraphic(Images.COG.imageView());

        emptyPane = createEmptyPane();

        setContent(emptyPane);

        tabPaneResultSets.getTabs().addListener((ListChangeListener<Tab>) c -> {
            if (c.getList().size() <= 0) {
                setContent(emptyPane);
            }
        });
    }

    private Node createEmptyPane() {
        BorderPane pane = new BorderPane();
        pane.setCenter(new Label("Select a query on the left side."));

        return pane;
    }

    public void executeQuery(final Query query, final Map<String, Object> columnMap) {
        logger.debug("Executing drilldown query '{}' with column map of {} keys", query.getName(), columnMap.size());

        NamedParameterPreparedStatement npsm = null;
        ResultSet rs = null;
        try {
            npsm = NamedParameterPreparedStatement.createNamedParameterPreparedStatement(connection, query.getContent());
            npsm.setMaxRows(100);
            if (columnMap.isEmpty()) {
                if (npsm.hasNamedParameters()) {
                    Map<String, String> map = new HashMap<>();
                    for (String s : npsm.getParameters()) {
                        TextInputDialog tid = new TextInputDialog();
                        tid.setHeaderText("Specify input for '" + s + "'");
                        Optional<String> opt = tid.showAndWait();
                        if (!opt.isPresent()) {
                            // bail out. User pressed cancel button.
                            return;
                        } else {
                            map.put(s, opt.get());
                        }
                    }


                    for (String key : map.keySet()) {
                        npsm.setObject(key, map.get(key));
                    }
                }
            } else {
                for (String key : columnMap.keySet()) {
                    npsm.setObject(key, columnMap.get(key));
                }
            }

            setContent(tabPaneResultSets);

            rs = npsm.executeQuery();
            DrillResultTable drt = new DrillResultTable(this, query);
            drt.populate(rs);

            Tab tab = new Tab(query.getName());
            tab.setContent(drt);
            tabPaneResultSets.getTabs().add(tab);
            tabPaneResultSets.getSelectionModel().select(tab);

        } catch (SQLDataException e) {
            logger.error("Invalid data given to statement", e);
            Utils.showExceptionDialog("SQL data exception", "The input given was invalid for the property.", e);
        } catch (SQLException e) {
            logger.error("Could not prepare named parameter statement", e);
            Utils.showExceptionDialog("Generic SQL exception", "See stacktrace below for details.", e);
        } finally {
            try {
                Utils.close(npsm);
                logger.debug("Closed statement");
                Utils.close(rs);
                logger.debug("Closed resultset");
            } catch (SQLException ex) {
                // swallow
            }
        }
    }
}
