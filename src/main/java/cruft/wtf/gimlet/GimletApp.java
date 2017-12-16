package cruft.wtf.gimlet;

import cruft.wtf.gimlet.conf.QueryConfiguration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class GimletApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem fileItemOne = new MenuItem("Exit");
        fileItemOne.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        fileItemOne.setOnAction(event -> Platform.exit());

        MenuItem fileItemTwo = new MenuItem("Add item");
        fileItemTwo.setAccelerator(KeyCombination.keyCombination("Ctrl+L"));
        fileItemTwo.setOnAction(event -> {
            AddEvent e = new AddEvent();
            e.setName("Kevin!");
            EventDispatcher.getInstance().post(e);
        });

        menuFile.getItems().add(fileItemOne);
        menuFile.getItems().add(fileItemTwo);

        Menu menuHelp = new Menu("Help");

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuHelp);
        return menuBar;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        BorderPane pane = new BorderPane();

        ProjectTree tree = new ProjectTree();
        try {
            QueryConfiguration c = QueryConfiguration.read(GimletApp.class.getResourceAsStream("/query-configuration.xml"));
            tree.setQueryConfiguration(c);
        } catch (JAXBException e) {
            e.printStackTrace();
            Platform.exit();
        }


        pane.setTop(createMenuBar());
        pane.setCenter(tree);

        Scene scene = new Scene(pane);

        primaryStage.setScene(scene);
        primaryStage.setWidth(320);
        primaryStage.setHeight(240);
        primaryStage.setTitle("Gimlet");
        primaryStage.show();
    }

}
