package cruft.wtf.gimlet.ui;


import cruft.wtf.gimlet.Column;
import cruft.wtf.gimlet.jdbc.task.SimpleQueryTask;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.List;

/**
 * This class represents a generic {@link TableView} for SQL queries. The table columns are therefore variadic depending
 * on the query executed. The table expects a {@link ResultSet} to iterate over. The class is not responsible for closing
 * the resources, merely displaying the data.
 *
 * @see SimpleQueryTask
 */
public class ResultTable extends TableView<ObservableList> {

    private static Logger logger = LoggerFactory.getLogger(ResultTable.class);


    public ResultTable() {
        setEditable(false);
        // FIXME: table menu button shows columns, but mnemonic is parsed (_). Disable that somehow.
        setTableMenuButtonVisible(true);
        setPlaceholder(new Label("Query is running..."));
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Sets the placeholder to mention that there are no results found.
     */
    public void setPlaceHolderNoResults() {
        setPlaceholder(new Label("No results for query."));
    }

    /**
     * Sets the columns.
     * <p>
     * TODO: since we now got a SimpleObjectProperty, do we really need the Column type? Maybe later.
     *
     * @param columnList The list of columns.
     */
    @SuppressWarnings("unchecked")
    public void setColumns(List<Column> columnList) {
        for (int i = 0; i < columnList.size(); i++) {
            final int colnum = i;
            TableColumn<ObservableList, Object> col = new TableColumn<>(columnList.get(colnum).getColumnName());
            // every column has a type dependent on the ResultSet. So just use SimpleObjectProperty.
            col.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().get(colnum)));
            col.setCellFactory(param -> new ResultSetCell());
            getColumns().add(col);
        }
    }
}
