package cruft.wtf.gimlet.ui;


import cruft.wtf.gimlet.TimedTask;
import cruft.wtf.gimlet.Utils;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is a tab where simple, plain SQL statements can be entered.
 */
public class SQLTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(SQLTab.class);

    /**
     * Parent {@link ConnectionTab}.
     */
    private final ConnectionTab connectionTab;

    /**
     * The SQL connection. Delegate from the connection tab.
     */
    private Connection connection;


    private TextArea txtQuery = new TextArea("select * from customer cross join invoice");

    private TabPane tabPaneResultSets = new TabPane();


    /**
     * Creates the {@link SQLTab} with the given {@link ConnectionTab} parent.
     *
     * @param connectionTab The tab.
     */
    public SQLTab(final ConnectionTab connectionTab) {
        this.connectionTab = connectionTab;
        this.connection = connectionTab.getConnection();

        setText("SQL");
        setClosable(false);
        setGraphic(Images.PULSE.imageView());

        txtQuery.setWrapText(false);
        txtQuery.setPromptText("Enter any SQL query here");
        txtQuery.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                logger.debug("Executing query...");
                executeQuery();
            }
        });

        SplitPane pane = new SplitPane();
        pane.setOrientation(Orientation.VERTICAL);
        pane.setDividerPosition(0, 0.5);
        pane.getItems().add(txtQuery);
        pane.getItems().add(tabPaneResultSets);

        setContent(pane);
    }

    /**
     * Executes the query in another thread (so the UI will not hang) and displays it in a new {@link ResultTable} in a
     * new {@link Tab}.
     */
    private void executeQuery() {
        TimedTask<Void> task = new TimedTask<Void>() {

            @Override
            protected Void call() throws Exception {
                String query = txtQuery.getText();
                // Check if we selected some text. If so, that's the query we want to run.
                if (txtQuery.getSelection().getLength() != 0) {
                    query = txtQuery.getSelectedText();
                }

                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    ResultTable table = new ResultTable();

                    Tab tab = new Tab(query);
                    tab.setContent(table);
                    Platform.runLater(() -> tabPaneResultSets.getTabs().add(tab));

                    stmt = connection.prepareStatement(query);
                    stmt.setMaxRows(100);
                    rs = stmt.executeQuery();

                    table.populate(stmt.executeQuery());

                } catch (SQLException e) {
                    logger.error("SQL exception", e);
                    Platform.runLater(() -> Utils.showExceptionDialog("SQL exception occurred.", "See exception below for more details.", e));
                } finally {
                    Utils.close(rs);
                    Utils.close(stmt);
                }

                return null;
            }
        };


        Thread t = new Thread(task, "Gimlet SQL thread");
        t.setDaemon(true);
        t.start();

    }
}
