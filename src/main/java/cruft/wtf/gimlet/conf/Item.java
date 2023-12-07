package cruft.wtf.gimlet.conf;

import jakarta.xml.bind.annotation.XmlType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * A configuration item which holds some common properties.
 *
 * @see Alias
 * @see Query
 */
@XmlType(propOrder = {
        "name",
        "description",
        "color",
        "colorDisabled"
})
public abstract class Item {

    private StringProperty name = new SimpleStringProperty();

    private StringProperty description = new SimpleStringProperty();

    private StringProperty color = new SimpleStringProperty("#c0c0c0");

    private BooleanProperty colorDisabled = new SimpleBooleanProperty(true);

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getColor() {
        return color.get();
    }

    public StringProperty colorProperty() {
        return color;
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    public boolean isColorDisabled() {
        return colorDisabled.get();
    }

    public BooleanProperty colorDisabledProperty() {
        return colorDisabled;
    }

    public void setColorDisabled(boolean colorDisabled) {
        this.colorDisabled.set(colorDisabled);
    }
}
