package cruft.wtf.gimlet.ui;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

/**
 * The {@link AliasPane} is nothing more than a container for the {@link AliasList} and a toolbar containing
 * buttons for actions on the list itself.
 */
public class AliasPane extends BorderPane {

    private AliasList aliasList;

    public AliasPane() {
        this.aliasList = new AliasList();

        Button btnUp = new Button("", Images.ARROW_UP.imageView());
        btnUp.setOnAction(event -> this.aliasList.moveAlias(Direction.UP));
        btnUp.setTooltip(new Tooltip("Move selected alias up"));

        Button btnDown = new Button("", Images.ARROW_DOWN.imageView());
        btnDown.setOnAction(event -> this.aliasList.moveAlias(Direction.DOWN));
        btnDown.setTooltip(new Tooltip("Move selected alias down"));

        ToolBar bar = new ToolBar(btnUp, btnDown);

        setTop(bar);
        setCenter(this.aliasList);
    }
}
