package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

/**
 * The {@link AliasPane} is nothing more than a container for the {@link AliasList} and a toolbar containing
 * buttons for actions on the list itself.
 */
public class AliasPane extends BorderPane {

    private AliasList aliasList;

    public AliasPane() {
        this.aliasList = new AliasList();

        Button btnUp = new Button();
        btnUp.setGraphic(Images.ARROW_UP.imageView());
        btnUp.setOnAction(event -> {
            this.aliasList.moveAlias(-1);
        });

        Button btnDown = new Button();
        btnDown.setGraphic(Images.ARROW_DOWN.imageView());
        btnDown.setOnAction(event -> this.aliasList.moveAlias(1));

        ToolBar bar = new ToolBar(btnUp, btnDown);

        setTop(bar);
        setCenter(this.aliasList);

        EventDispatcher.getInstance().register(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onFileOpened(final FileOpenedEvent foe) {
        aliasList.setAliases(foe.getGimletProject().aliasesProperty());
    }
}
