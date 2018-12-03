package cruft.wtf.gimlet.ui.controls;

import javafx.scene.control.TextField;

import java.util.Map;
import java.util.Set;

/**
 * Basically this is just a specialization of a {@link TextField}, except it satisfies the {@link ParamInput}
 * interface so it can be transparently used by the {@link cruft.wtf.gimlet.ui.dialog.ParamInputDialog}.
 *
 * @see cruft.wtf.gimlet.ui.dialog.ParamInputDialog#ParamInputDialog(String, Set)
 * @see cruft.wtf.gimlet.ui.dialog.ParamInputDialog#prefill(Map)
 */
public class StringTextField extends TextField implements ParamInput {
    @Override
    public String getParameterName() {
        return getId();
    }

    @Override
    public Object getParameterValue() {
        return getText();
    }

    @Override
    public void setParameterValue(Object o) {
        setText(String.valueOf(o));
    }
}
