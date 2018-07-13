package cruft.wtf.gimlet.ui;

import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.EventDispatcher;
import cruft.wtf.gimlet.event.FileOpenedEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

public class QueryPane extends BorderPane {

    private QueryTree queryTree;

    public QueryPane() {
        queryTree = new QueryTree();

        Button btnMoveUp = new Button("", Images.ARROW_UP.imageView());
        btnMoveUp.setOnAction(event -> queryTree.moveSelectedNode(Direction.UP));
        btnMoveUp.setTooltip(new Tooltip("Move the selected query one sibling up"));

        Button btnMoveDown = new Button("", Images.ARROW_DOWN.imageView());
        btnMoveDown.setOnAction(event -> queryTree.moveSelectedNode(Direction.DOWN));
        btnMoveDown.setTooltip(new Tooltip("Move the selected query one sibling down"));

        ToolBar toolBar = new ToolBar(btnMoveUp, btnMoveDown);

        setTop(toolBar);
        setCenter(queryTree);

        EventDispatcher.getInstance().register(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onFileOpened(final FileOpenedEvent foe) {
        queryTree.setQueryList(foe.getGimletProject().queriesProperty());
        queryTree.setProject(foe.getGimletProject());
    }
}
