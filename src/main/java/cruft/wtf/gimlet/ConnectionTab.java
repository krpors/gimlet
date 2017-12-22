package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.Alias;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;

import java.sql.*;

public class ConnectionTab extends Tab {

    private final Alias alias;

    private Connection connection;

    public ConnectionTab(final Alias alias) throws SQLException {
        this.alias = alias;

        setGraphic(Images.BOLT.imageView());
        setText(alias.getName());

        connection = DriverManager.getConnection(alias.getUrl(), alias.getUser(), alias.getPassword());

        setOnCloseRequest(e -> {
            try {
                connection.close();
                System.out.println("Connection closed.");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        Button b = new Button("CLIX ME!");
        b.setOnAction(e -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from customer");
                 ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                    System.out.println(rs.getString(2));
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
        setContent(b);
    }

}
