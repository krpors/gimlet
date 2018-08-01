package cruft.wtf.gimlet.ui.drilldown;


import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.ui.ResultTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a specialization of the regular {@link ResultTable}. The main difference is that this class allows us
 * to 'drill down'.
 */
public class DrillResultTable extends ResultTable {


    public DrillResultTable(final Query query) {
        setRowFactory(param -> new DrillResultTableRow(query));
    }

}
