package cruft.wtf.gimlet.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectsTab extends Tab {

    private Connection connection;

    public ObjectsTab() {
        setText("Database Objects");
        setGraphic(Images.DOCUMENT.imageView());
        setClosable(false);
        setContent(new Label(":D"));
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void doit() {
        try {
            DatabaseMetaData dmd = connection.getMetaData();
            ResultSet tables = dmd.getTables(null, null, "%", null);
            while (tables.next()) {
                if (tables.getString("TABLE_TYPE").equals("TABLE")) {
                    System.out.println(tables.getString("TABLE_NAME"));
                    ResultSet  cols = dmd.getColumns(null, null, tables.getString("TABLE_NAME"), "%");
                    while(cols.next()) {
                        System.out.println("\t" + cols.getString("COLUMN_NAME") + " -> " + cols.getString("DATA_TYPE"));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
