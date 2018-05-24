package cruft.wtf.gimlet.ui;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class FormPane extends GridPane {

    private int rowCounter = 0;

    public FormPane() {
        setHgap(10);
        setVgap(5);
        setPadding(new Insets(10));

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setMinWidth(90);
        col2.setHgrow(Priority.ALWAYS);

        getColumnConstraints().addAll(col1, col2);
    }

    public void add(String label, Node node) {
        Label lbl = new Label(label);
        GridPane.setValignment(lbl, VPos.CENTER);
        lbl.setMnemonicParsing(true);
        lbl.setLabelFor(node);
        super.add(lbl, 0, rowCounter);
        super.add(node, 1, rowCounter);
        rowCounter++;
    }

}
