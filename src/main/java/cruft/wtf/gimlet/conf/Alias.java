package cruft.wtf.gimlet.conf;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {
        "name",
        "description",
        "url",
        "driverClass",
        "user",
        "password",
        "askForPassword",
        "color",
        "colorDisabled"
})
public class Alias {

    private StringProperty name = new SimpleStringProperty();

    private StringProperty description = new SimpleStringProperty();

    private StringProperty url = new SimpleStringProperty();

    private StringProperty driverClass = new SimpleStringProperty();

    private StringProperty user = new SimpleStringProperty();

    private StringProperty password = new SimpleStringProperty(); // TODO: byte array

    private StringProperty color = new SimpleStringProperty("#c0c0c0");

    private BooleanProperty colorDisabled = new SimpleBooleanProperty(true);

    private BooleanProperty askForPassword = new SimpleBooleanProperty(false);

    @XmlElement(name = "name")
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

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    @XmlElement(name = "driver-class")
    public String getDriverClass() {
        return driverClass.get();
    }

    public StringProperty driverClassProperty() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass.set(driverClass);
    }

    public String getUser() {
        return user.get();
    }

    public StringProperty userProperty() {
        return user;
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
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

    public boolean isAskForPassword() {
        return askForPassword.get();
    }

    public BooleanProperty askForPasswordProperty() {
        return askForPassword;
    }

    public void setAskForPassword(boolean askForPassword) {
        this.askForPassword.set(askForPassword);
    }

    @Override
    public String toString() {
        return "Alias{" +
                "name=" + name +
                ", description=" + description +
                ", url=" + url +
                ", user=" + user +
                ", password=" + password +
                '}';
    }
}
