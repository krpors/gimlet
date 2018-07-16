package cruft.wtf.gimlet.conf;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.TreeMap;

@XmlRootElement(name = "alias") // only use for tests
@XmlType(propOrder = {
        "url",
        "driverClass",
        "user",
        "password",
        "askForPassword",
        "readOnly",
        "jdbcProperties",
        "query"
})
@XmlSeeAlso({Item.class})
public class Alias extends Item {

    private StringProperty url = new SimpleStringProperty();

    private StringProperty driverClass = new SimpleStringProperty();

    private StringProperty user = new SimpleStringProperty();

    private StringProperty password = new SimpleStringProperty(); // TODO: byte array

    private BooleanProperty askForPassword = new SimpleBooleanProperty(false);

    private StringProperty query = new SimpleStringProperty();

    private BooleanProperty readOnly = new SimpleBooleanProperty(true);

    private MapProperty<String, String> jdbcProperties = new SimpleMapProperty<>(FXCollections.observableMap(new TreeMap<>()));

    public Alias() {
    }

    /**
     * Copy constructor. Copies everything, except that it renames the Alias' name.
     *
     * @param other The content to copy from.
     */
    public Alias(final Alias other) {
        copyFrom(other);
        setName(getName() + " (copy)");
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

    public boolean isAskForPassword() {
        return askForPassword.get();
    }

    public BooleanProperty askForPasswordProperty() {
        return askForPassword;
    }

    public void setAskForPassword(boolean askForPassword) {
        this.askForPassword.set(askForPassword);
    }

    public String getQuery() {
        return query.get();
    }

    public StringProperty queryProperty() {
        return query;
    }

    public void setQuery(String query) {
        this.query.set(query);
    }

    public ObservableMap<String, String> getJdbcProperties() {
        return jdbcProperties.get();
    }

    public MapProperty<String, String> jdbcPropertiesProperty() {
        return jdbcProperties;
    }

    public void setJdbcProperties(ObservableMap<String, String> jdbcProperties) {
        this.jdbcProperties.set(jdbcProperties);
    }

    public boolean isReadOnly() {
        return readOnly.get();
    }

    public BooleanProperty readOnlyProperty() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly.set(readOnly);
    }

    /**
     * Copies values over from another {@link Alias}. This is to keep the reference intact.
     *
     * @param other The values to use to copy to this instance.
     * @see cruft.wtf.gimlet.ui.dialog.AliasDialog
     */
    public void copyFrom(final Alias other) {
        setAskForPassword(other.isAskForPassword());
        setColor(other.getColor());
        setColorDisabled(other.isColorDisabled());
        setDescription(other.getDescription());
        setDriverClass(other.getDriverClass());
        setName(other.getName());
        setPassword(other.getPassword());
        setQuery(other.getQuery());
        setUrl(other.getUrl());
        setUser(other.getUser());
        setJdbcProperties(other.getJdbcProperties());
        setReadOnly(other.isReadOnly());
    }

    @Override
    public String toString() {
        return "Alias{" +
                "name=" + nameProperty().get() +
                ", description=" + descriptionProperty().get() +
                ", url=" + url +
                ", user=" + user +
                ", password=" + password +
                '}';
    }
}
