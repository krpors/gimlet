package cruft.wtf.gimlet.jdbc;

import cruft.wtf.gimlet.ui.DatabaseObject;
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
    private TreeView<DatabaseObject> treeView;

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

    public ObjectLoaderTask(TreeView<DatabaseObject> tree, Connection connection) {
        this.connection = connection;
        this.treeView = tree;
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
        TreeItem<DatabaseObject> root = new TreeItem<>(new DatabaseObject(DatabaseObject.ROOT, "Tables"));

        Platform.runLater(() -> treeView.setRoot(root));

        findSchemas(root);

        return null;
    }

    private void findSchemas(final TreeItem<DatabaseObject> root) throws SQLException {
        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet rs = dmd.getSchemas();
        while (rs.next()) {
            if (isCancelled()) {
                break;
            }

            String schema = rs.getString("TABLE_SCHEM");
            loadingSchemaProperty.set("Loading schema " + schema);

            TreeItem<DatabaseObject> item = new TreeItem<>(new DatabaseObject(DatabaseObject.SCHEMA, schema));
            Platform.runLater(() -> root.getChildren().add(item));

            findTables(item, schema);
            if (item.getChildren().size() == 0) {
                // remove empty schemas
                root.getChildren().remove(item);
            }
        }

        rs.close();
    }

    private void findTables(final TreeItem<DatabaseObject> root, String schemaName) throws SQLException {
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

                TreeItem<DatabaseObject> treeItemTable = new TreeItem<>(new DatabaseObject(DatabaseObject.TABLE, tableName));
                Platform.runLater(() -> root.getChildren().add(treeItemTable));
                // findColumns(treeItemTable, tableName);
            }
        }

        tables.close();
    }

    private void findColumns(final TreeItem<DatabaseObject> root, String tableName) throws SQLException {
        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet cols = dmd.getColumns(null, null, tableName, "%");

        while (cols.next()) {
            if (isCancelled()) {
                break;
            }

            String colname = cols.getString("COLUMN_NAME");
            TreeItem<DatabaseObject> col = new TreeItem<>(new DatabaseObject(DatabaseObject.COLUMN, colname));
            Platform.runLater(() -> root.getChildren().add(col));
        }

        cols.close();
    }
}
