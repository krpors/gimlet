package cruft.wtf.gimlet;


import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

/**
 * This class represents a generic {@link TableView} for SQL queries. The table columns are therefore variadic depending
 * on the query executed. The table expects a {@link ResultSet} to iterate over. The class is not responsible for closing
 * the resources, merely displaying the data.
 */
public class DrillResultTable extends ResultTable {

    private static Logger logger = LoggerFactory.getLogger(DrillResultTable.class);

    public DrillResultTable() {
    }
}
