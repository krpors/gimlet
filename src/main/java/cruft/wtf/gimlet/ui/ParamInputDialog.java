package cruft.wtf.gimlet.ui;


import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Very simple dialog which displays a textfield for each parameter given in the Set.
 * The result type of the dialog is a Map of String mapped to Objects which can be used
 * as parameter values in named queries.
 *
 * TODO: somehow give data types to the queries so you can display a calendar, or a number field etc.
 */
public class ParamInputDialog extends Dialog<Map<String, Object>> {

    public ParamInputDialog(Set<String> paramNames) {
        setTitle("Input for query");
        setHeaderText("Specify input for the query:");
        setGraphic(Images.MAGNIFYING_GLASS.imageView());

        FormPane pane = new FormPane();
        paramNames.forEach(s -> {
            TextField tf = new TextField();
            tf.setId(s);
            pane.add(s + ":", tf);
        });

        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setWidth(320);
        setHeight(240);

        setResultConverter(btnType -> {
            if (btnType == ButtonType.OK) {
                // convert cruft to hashmap.
                Map<String, Object> map = new TreeMap<>();
                pane.getChildren()
                        .stream()
                        .filter(node -> node instanceof TextField)
                        .map(node -> (TextField) node)
                        .forEach(textField -> map.put(textField.getId(), textField.getText()));

                return map;
            }

            return null;
        });

        Platform.runLater(() -> pane.getChildren().get(1).requestFocus());
    }


}
