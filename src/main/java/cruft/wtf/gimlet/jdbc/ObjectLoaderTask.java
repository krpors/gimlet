package cruft.wtf.gimlet.jdbc;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This task is responsible for finding all database objects given a connection.
 */
public class ObjectLoaderTask extends Task<Void> {

    /**
     * The TreeView to add items to.
     */
    private TreeView<String> treeView;

    /**
     * The connection to use. Must be open, and non-null.
     */
    private Connection connection;

    /**
     * The property containing the string which schema is being loaded.
     */
    private StringProperty loadingSchemaProperty = new SimpleStringProperty("");

    /**
     * The property containing the string which table is being loaded.
     */
    private StringProperty loadingTableProperty = new SimpleStringProperty("");

    public ObjectLoaderTask(TreeView<String> treeView, Connection connection) {
        this.connection = connection;
        this.treeView = treeView;
    }

    public String getLoadingSchemaProperty() {
        return loadingSchemaProperty.get();
    }

    public StringProperty loadingSchemaPropertyProperty() {
        return loadingSchemaProperty;
    }

    public String getLoadingTableProperty() {
        return loadingTableProperty.get();
    }

    public StringProperty loadingTablePropertyProperty() {
        return loadingTableProperty;
    }

    /**
     * Starts the task.
     *
     * @return
     * @throws Exception
     */
    @Override
    protected Void call() throws Exception {
        TreeItem<String> root = new TreeItem<>("ROOT!");

        long start = System.currentTimeMillis();
        Platform.runLater(() -> treeView.setRoot(root));

        findSchemas(root);
        System.out.println("End: " + (System.currentTimeMillis() - start));
        return null;
    }

    private void findSchemas(final TreeItem<String> root) throws SQLException {
        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet rs = dmd.getSchemas();
        while (rs.next()) {
            if (isCancelled()) {
                break;
            }

            String schema = rs.getString("TABLE_SCHEM");
            loadingSchemaProperty.set("Loading schema " + schema);

            TreeItem<String> item = new TreeItem<>(schema);
            Platform.runLater(() -> root.getChildren().add(item));

            findTables(item, schema);
            if (item.getChildren().size() == 0) {
                // remove empty schemas
                root.getChildren().remove(item);
            }
        }
    }

    private void findTables(final TreeItem<String> root, String schemaName) throws SQLException {
        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet tables = dmd.getTables(null, schemaName, "%", null);
        while (tables.next()) {
            if (isCancelled()) {
                break;
            }

            String tableType = tables.getString("TABLE_TYPE");
            if ("TABLE".equals(tableType)) {
                String tableName = tables.getString("TABLE_NAME");
                loadingTableProperty.set("Loading table " + tableName);

                TreeItem<String> treeItemTable = new TreeItem<>(tableName);
                Platform.runLater(() -> root.getChildren().add(treeItemTable));
                findColumns(treeItemTable, tableName);
            }
        }
    }

    private void findColumns(final TreeItem<String> root, String tableName) throws SQLException {
        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet cols = dmd.getColumns(null, null, tableName, "%");

        while (cols.next()) {
            if (isCancelled()) {
                break;
            }

            String colname = cols.getString("COLUMN_NAME");
            TreeItem<String> col = new TreeItem<>(colname);
            Platform.runLater(() -> root.getChildren().add(col));

        }
    }
}
