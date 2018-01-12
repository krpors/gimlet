package cruft.wtf.gimlet.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;

public class ObjectsTab extends Tab {
    public ObjectsTab() {
        setText("Database Objects");
        setGraphic(Images.DOCUMENT.imageView());
        setClosable(false);
        setContent(new Label(":D"));
    }
}
