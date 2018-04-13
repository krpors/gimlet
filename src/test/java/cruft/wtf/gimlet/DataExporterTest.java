package cruft.wtf.gimlet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataExporterTest {

    private String[] colNames;
    private ObservableList<ObservableList> data;

    @Before
    public void init() {
        colNames = new String[]{"LARGE_FIRST_COLUMN_NAME", "Two"};

        data = FXCollections.observableArrayList();
        data.add(FXCollections.observableArrayList("Kevin", "Pors"));
        data.add(FXCollections.observableArrayList("Derpy", "Derpington"));
        data.add(FXCollections.observableArrayList("Cruft and all", "Jazzy newsflash"));
        data.add(FXCollections.observableArrayList("Ozzy", "Bozzy"));
        data.add(FXCollections.observableArrayList("This col", "The other column, contains\na newline!!"));
    }

    /**
     * This tests whether the fitWidth, includeColNames work, including a custom column separator and data
     * separator.
     */
    @Test
    public void case01() {
        DataExporter.Options opts = new DataExporter.Options();
        opts.fitWidth = true;
        opts.includeColNames = true;
        opts.columnSeparator = " |";
        opts.columnAndDataSeparator = '=';

        String what = DataExporter.exportToBasicString(colNames, data, opts);

        System.out.println(what);

        Assert.assertEquals(TestUtils.readFromClasspath("/exporter/case01.txt"), what);
    }

    @Test
    public void case02() {
        DataExporter.Options opts = new DataExporter.Options();
        opts.fitWidth = false;
        opts.includeColNames = true;
        opts.columnSeparator = "|";
        opts.columnAndDataSeparator = '*';

        String what = DataExporter.exportToBasicString(colNames, data, opts);

        System.out.println(what);

        Assert.assertEquals(TestUtils.readFromClasspath("/exporter/case02.txt"), what);
    }

    @Test
    public void case03() {
        DataExporter.Options opts = new DataExporter.Options();
        opts.fitWidth = false;
        opts.includeColNames = false;
        opts.columnSeparator = ";";

        String what = DataExporter.exportToBasicString(colNames, data, opts);

        System.out.println(what);

        Assert.assertEquals(TestUtils.readFromClasspath("/exporter/case03.txt"), what);
    }


}