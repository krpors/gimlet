package cruft.wtf.gimlet.ui.controls;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 * This is a simple menu item for separation, but instead of using a horizontal
 * rule, we use a label.
 */
public class LabeledSeparatorMenuItem extends CustomMenuItem {

    private Label lbl;

    public LabeledSeparatorMenuItem(String text) {
        // We must give the item, AND the label the same class
        // or else we get the (blue) highlighting as well of the item!
        getStyleClass().add("labeledMenuItem");

        setHideOnClick(false);
        lbl = new Label(text);
        lbl.getStyleClass().add("labeledMenuItem");
        setContent(lbl);
    }

    public void setTooltip(Tooltip tooltip) {
        lbl.setTooltip(tooltip);
    }
}
