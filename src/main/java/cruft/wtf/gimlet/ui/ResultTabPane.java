package cruft.wtf.gimlet.ui;

import cruft.wtf.gimlet.jdbc.Column;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;

import java.util.List;

/**
 * This TabPane contains tabs with a {@link ResultTable}, {@link RotatedTable}, and metadata
 * about the resultset.
 */
public class ResultTabPane extends TabPane {

    private ResultTable resultTable;

    private RotatedTable rotatedTable;

    // TODO: metadata about the resultset.
    private Node metaData;

    private final ConnectionTab connectionTab;

    public ResultTabPane(final ConnectionTab connectionTab) {
        this.connectionTab = connectionTab;
        this.resultTable = new ResultTable();
        this.rotatedTable = new RotatedTable();
        this.metaData = new Text("MetaData comes here");

        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        Tab tabResult = new Tab("Data", resultTable);
        Tab tabRotated = new Tab("Rotated table", rotatedTable);
        tabRotated.setOnSelectionChanged(event -> {
            if (tabRotated.isSelected()) {
                // start loading!
//                List<Column>
                Task task = new Task() {
                    @Override
                    protected Void call() throws Exception {
                        // TODO: Rotating a table with for ex. 2500 items TAKES A SHITLOAD OF TIME.
                        // The rotation itself is rather quick, but adding so many columns.
                        Platform.runLater(() -> rotatedTable.setItems(resultTable.getColumnList(), resultTable.getItems()));
                        return null;
                    }
                };
                Thread t = new Thread(task);
                t.setDaemon(true);
                t.start();
            }
        });

        getTabs().addAll(tabResult, tabRotated);
    }

    public void setItems(List<Column> columnList, ObservableList<ObservableList> rowData) {
        resultTable.setItems(columnList, rowData);
    }
}
