package cruft.wtf.gimlet;

import com.google.common.eventbus.Subscribe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class SomeList extends ListView<String> {

    public SomeList() {
        ObservableList<String> s = FXCollections.observableArrayList();
        s.add("Hi!");
        s.add("Derp!");
        setItems(s);

        setOnMouseClicked(event -> {
            System.out.println("Clixed on " + getSelectionModel().getSelectedItem());
        });

        EventDispatcher.getInstance().register(this);
    }

    @Subscribe
    public void stuff(AddEvent event) {
        System.out.println("Add0r");
        getItems().add(event.getName());
    }
}
