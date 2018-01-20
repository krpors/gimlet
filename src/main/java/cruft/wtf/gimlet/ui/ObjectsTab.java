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
                if (tables.getString(4).equals("TABLE")) {
                    System.out.println(tables.getString(3));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
