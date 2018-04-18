package cruft.wtf.gimlet;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A basic effort for a window with some information about the application.
 */
public class AboutWindow extends Stage {

    public AboutWindow(final Application application) {
        initOwner(GimletApp.window);
        initStyle(StageStyle.UNDECORATED);
        setWidth(640);
        setHeight(400);
        setTitle("About Gimlet");
        setAlwaysOnTop(true);
        centerOnScreen();

        ImageView imageView = new ImageView("/splash.png");

        String s = "A simple JDBC based query evaluator.\n\n";
        s += "For more information, or for feedback, bugs, issues, feature requests\n";
        s += "be sure to check out https://github.com/krpors/gimlet.\n\n";

        Label lbl = new Label(s);
        lbl.setStyle("-fx-text-fill: white");
        lbl.setLayoutX(45);
        lbl.setLayoutY(120);

        Hyperlink link = new Hyperlink("Project homepage");
        link.setOnAction(event -> {
            HostServices services = application.getHostServices();
            services.showDocument("https://github.com/krpors/gimlet/");

        });
        link.setLayoutX(45);
        link.setLayoutY(360);

        AnchorPane pane = new AnchorPane(imageView, lbl, link);
        pane.setOnMouseClicked(event -> close());

        Scene scene = new Scene(pane);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });
        setScene(scene);

    }
}
