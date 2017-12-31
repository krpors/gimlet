package cruft.wtf.gimlet;


import cruft.wtf.gimlet.event.QueryExecutedEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * This class represents a generic {@link TableView} for SQL queries. The table columns are therefore variadic depending
 * on the query executed.
 */
public class ResultTable extends TableView {

    private static Logger logger = LoggerFactory.getLogger(ResultSet.class);

    private int rowCount = 0;

    public ResultTable() {
        setEditable(false);
        setTableMenuButtonVisible(true);
        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        setPlaceholder(new Label("Query is running..."));
    }

    /**
     * Executes the given {@code query} on the {@code connection} and displays the result in this table.
     *
     * @param connection The SQL connection to operate on.
     * @param query      The query to actually execute.
     */
    public void executeAndPopulate(final Connection connection, final String query) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setMaxRows(0);
            logger.debug("Executing query...");
            long start = System.currentTimeMillis();
            rs = statement.executeQuery();
            long end = System.currentTimeMillis();
            logger.debug("Done!");
            populate(rs);

            QueryExecutedEvent qee = new QueryExecutedEvent();
            qee.setRowCount(getRowCount());
            qee.setQuery(query);
            qee.setRuntime(end - start);
            EventDispatcher.getInstance().post(qee);
        } catch (SQLException ex) {
            logger.error("Could not execute query", ex);
            Platform.runLater(() -> {
                setPlaceholder(new Label("Query failed!"));
                Utils.showExceptionDialog("Could not execute query", "Query failed", ex);
            });
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    logger.debug("ResultSet closed");
                }
                if (statement != null) {
                    statement.close();
                    logger.debug("Statement closed");
                }

            } catch (SQLException ex) {
                logger.error("Could not close JDBC resources", ex);
                Platform.runLater(() -> Utils.showExceptionDialog("Could not close JDBC resources ourselves.", "Whoops!", ex));
            }
        }
    }

    /**
     * Populates the table with the {@link ResultSet}. It is expected that this class is forwarding the cursor. Updating
     * the UI happens via the {@link Platform#runLater(Runnable)} utility function.
     *
     * @param rs The {@link ResultSet}.
     * @throws SQLException When iterating the resultset fails for whatever reason.
     */
    @SuppressWarnings("unchecked")
    private void populate(final ResultSet rs) throws SQLException {
        rowCount = 0;

        Platform.runLater(() -> {
            getColumns().clear();
            getItems().clear();
        });

        ResultSetMetaData rsmd = rs.getMetaData();
        logger.debug("Resultset contains {} columns", rsmd.getColumnCount());


        TableColumn<ObservableList, Number> idCol = new TableColumn("#");
        idCol.setCellValueFactory(param -> new SimpleIntegerProperty((Integer) param.getValue().get(0)));
        Platform.runLater(() -> getColumns().add(idCol));

        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            final int j = i + 1;

            // This construction allows us to make the table sortable by numbers. Need to figure out though
            // if this needs a lot of expansion for all other types of integer-like column types.
            //
            // The reference to the actual data is a bit funky: it refers to the ObservableList containing the
            // row data (via setItems).
            if (rsmd.getColumnType(i + 1) == Types.INTEGER) {
                TableColumn<ObservableList, Number> column = new TableColumn<>(rsmd.getColumnName(i + 1));
                column.setCellValueFactory(param -> {
                    System.out.println(param.getValue());
                    return new SimpleIntegerProperty((Integer) param.getValue().get(j));
                });
                Platform.runLater(() -> getColumns().add(column));
            } else {
                TableColumn<ObservableList, String> col = new TableColumn<>(rsmd.getColumnName(i + 1));
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                Platform.runLater(() -> getColumns().add(col));
            }
        }

        ObservableList<ObservableList> rowdata = FXCollections.observableArrayList();
        while (rs.next()) {
            rowCount++;
            ObservableList werd = FXCollections.observableArrayList();
            werd.add(rowCount);
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                // first element is the actual row number, which is just the i counter;

                if (rsmd.getColumnType(i) == Types.INTEGER) {
                    werd.add(rs.getInt(i));
                } else {
                    werd.add(rs.getString(i));
                }
            }

            rowdata.add(werd);
        }

        // Err... to prevent that the clearing of the items is done later than the setting, we also run the setting
        // of the items via Platform.runLater. This looks hacky as fuck but we'll manage for now.
        Platform.runLater(() -> {
            setItems(rowdata);
        });
    }

    /**
     * Returns the rowcount after the ResultTable has been populated by {@link #populate(ResultSet)}.
     *
     * @return The rowcount.
     */
    public int getRowCount() {
        return rowCount;
    }
}
