package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.AliasConfiguration;
import cruft.wtf.gimlet.conf.QueryConfiguration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GimletApp extends Application {

    public static Connection sqlConnection;

    public static void main(String[] args) {
        launch(args);
    }

    public MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem fileItemOne = new MenuItem("Exit");
        fileItemOne.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        fileItemOne.setOnAction(event -> Platform.exit());

        menuFile.getItems().add(fileItemOne);

        Menu menuHelp = new Menu("Help");

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuHelp);
        return menuBar;
    }

    private Node createAccordion() {
        Accordion accordion = new Accordion();

        AliasTable tbl = new AliasTable();
        try {
            AliasConfiguration ac = AliasConfiguration.read(GimletApp.class.getResourceAsStream("/alias-configuration.xml"));
            tbl.setAliases(ac);
        } catch (JAXBException e) {
            e.printStackTrace();
            Platform.exit();
        }

        QueryConfigurationTree tree = new QueryConfigurationTree();
        try {
            QueryConfiguration c = QueryConfiguration.read(GimletApp.class.getResourceAsStream("/queries.xml"));
            tree.setQueryConfiguration(c);
        } catch (JAXBException e) {
            e.printStackTrace();
            Platform.exit();
        }

        TitledPane pane1 = new TitledPane("Aliases", tbl);
        TitledPane pane2 = new TitledPane("Queries", tree);

        accordion.getPanes().addAll(pane1, pane2);


        return accordion;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            sqlConnection = DriverManager.getConnection("jdbc:hsqldb:file:/home/krpors/Development/hsql", "admin", "admin");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        BorderPane pane = new BorderPane();

        pane.setTop(createMenuBar());
        pane.setLeft(createAccordion());
        pane.setCenter(new EditorTabView());

        Scene scene = new Scene(pane);

        primaryStage.setScene(scene);
        primaryStage.setWidth(640);
        primaryStage.setHeight(480);
        primaryStage.setTitle("Gimlet");
        primaryStage.show();
    }

}
