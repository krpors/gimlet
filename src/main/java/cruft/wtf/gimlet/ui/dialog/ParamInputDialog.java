package cruft.wtf.gimlet.ui.dialog;


import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.jdbc.ParseResult;
import cruft.wtf.gimlet.ui.FormPane;
import cruft.wtf.gimlet.ui.Images;
import cruft.wtf.gimlet.ui.controls.NumberTextField;
import javafx.application.Platform;
import javafx.scene.Node;
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
 * <p>
 * TODO: somehow give data types to the queries so you can display a calendar, or a number field etc.
 */
public class ParamInputDialog extends Dialog<Map<String, Object>> {

    private FormPane pane;

    public ParamInputDialog(Set<ParseResult.Param> paramNames) {
        initOwner(GimletApp.window);
        setTitle("Input for query");
        setHeaderText("Specify input for the query:");
        setGraphic(Images.MAGNIFYING_GLASS.imageView());

        pane = new FormPane();
        paramNames.forEach(k -> {
            addNode(pane, k);
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

    /**
     * Based on the given map, prefill the textfields in this dialog. The ID of the textfield
     * is used to map the value of the {@code values} map. In other words, if a key in the map
     * is {@code SOME_KEY}, the textfield with the ID @{code SOME_KEY} will be given the value
     * of that key in the map.
     *
     * @param values The values to prefill this input dialog with. Null values are ignored.
     */
    public void prefill(final Map<String, Object> values) {
        pane.getChildren()
                .stream()
                // Only act on Nodes which are TextFields.
                .filter(node -> node instanceof TextField)
                // Force casting so the forEach will work out.
                .map(node -> (TextField) node)
                // Iterate over all TextFields. Get the ID, and then the same ID from the
                // values map.
                .forEach(textField -> {
                    String id = textField.getId();
                    Object value = values.get(id);
                    if (value != null) {
                        textField.setText(String.valueOf(value));
                    }
                });
    }


    /**
     * Adds a node based on the {@link ParseResult.Param}.
     *
     * @param parent The parent form pane.
     * @param param  The parameter to create a node for.
     */
    private void addNode(final FormPane parent, final ParseResult.Param param) {
        Node n = null;
        switch (param.getDataType()) {
            // TODO: create different types of nodes for different parameters.
            case NUMBER:
                n = new NumberTextField();
                break;
            default:
                n = new TextField();
                break;
        }

        // Set the ID of the Node, based on the parameter name. This is used
        // so we can prefill them when necessary.
        n.setId(param.getName());

        parent.add(param.getName() + ":", n);
    }

}
