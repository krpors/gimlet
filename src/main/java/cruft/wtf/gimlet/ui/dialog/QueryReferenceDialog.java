package cruft.wtf.gimlet.ui.dialog;

import cruft.wtf.gimlet.GimletApp;
import cruft.wtf.gimlet.conf.Query;
import cruft.wtf.gimlet.ui.BasicQueryTree;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.List;

/**
 * The dialog select a query reference in. This class makes use of a slimmed-down specialized {@link cruft.wtf.gimlet.ui.QueryTree},
 * actually called the {@link BasicQueryTree}. That control just has the logic to display the query tree and select
 * one out of it.
 */
public class QueryReferenceDialog extends Dialog<Query> {

    private BasicQueryTree basicQueryTree;

    public QueryReferenceDialog(final List<Query> queryList) {
        initOwner(GimletApp.window);
        setTitle("Select query reference");
        setHeaderText("Select a query to make a reference to.");
        getDialogPane().setContent(createContent());

        basicQueryTree.setQueryList(queryList);

        getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL);

        setResultConverter(btnType -> {
            if (btnType == ButtonType.OK) {
                return basicQueryTree.getSelected().orElse(null);
            }
            return null;
        });
    }

    private Node createContent() {
        basicQueryTree = new BasicQueryTree();
        return basicQueryTree;
    }
}

