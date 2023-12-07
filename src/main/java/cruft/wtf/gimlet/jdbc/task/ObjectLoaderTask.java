package cruft.wtf.gimlet.jdbc.task;

import cruft.wtf.gimlet.ui.objects.DatabaseObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This task is responsible for finding all database objects given a connection.
 */
public class ObjectLoaderTask extends Task<TreeItem<DatabaseObject>> {

    private static Logger logger = LoggerFactory.getLogger(ObjectLoaderTask.class);

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

    public ObjectLoaderTask(Connection connection) {
        this.connection = connection;
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
     * @return The root of a {@link javafx.scene.control.TreeView}.
     * @throws Exception Whenever something failed.
     */
    @Override
    protected TreeItem<DatabaseObject> call() throws Exception {
        TreeItem<DatabaseObject> root = new TreeItem<>(new DatabaseObject(DatabaseObject.ROOT, "Root", null));
        root.setExpanded(true);

        TreeItem<DatabaseObject> schemas = new TreeItem<>(new DatabaseObject(DatabaseObject.ROOT, "Schemas", null));
        findSchemas(schemas);

        TreeItem<DatabaseObject> noSchemas = new TreeItem<>(new DatabaseObject(DatabaseObject.ROOT, "No schema", null));
        findTables(noSchemas, null);

        root.getChildren().add(schemas);
        root.getChildren().add(noSchemas);

        return root;
    }

    private void findSchemas(final TreeItem<DatabaseObject> root) throws SQLException {
        DatabaseMetaData dmd = connection.getMetaData();
        logger.debug("Getting database metadata to determine schemas");
        ResultSet rs = dmd.getSchemas();
        while (rs.next()) {
            if (isCancelled()) {
                break;
            }

            String schema = rs.getString("TABLE_SCHEM");
            loadingSchemaProperty.set("Loading schema " + schema);
            logger.debug("Found schema '{}'", schema);

            TreeItem<DatabaseObject> item = new TreeItem<>(new DatabaseObject(DatabaseObject.SCHEMA, schema, null));
            root.getChildren().add(item);

            findTables(item, schema);
        }
        rs.close();
    }

    private void findTables(final TreeItem<DatabaseObject> root, String schemaName) throws SQLException {
        logger.info("Finding tables for schema '{}'", schemaName);
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
                logger.debug("Found table '{}' for schema '{}'", tableName, schemaName);

                TreeItem<DatabaseObject> treeItemTable = new TreeItem<>(new DatabaseObject(DatabaseObject.TABLE, schemaName, tableName));
                root.getChildren().add(treeItemTable);
            }
        }

        tables.close();
    }
}
