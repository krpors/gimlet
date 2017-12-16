package cruft.wtf.gimlet.conf;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "aliases")
public class AliasConfiguration implements Item {

    private StringProperty name = new SimpleStringProperty();

    private StringProperty description = new SimpleStringProperty();

    private SimpleListProperty<Alias> aliases = new SimpleListProperty<>(FXCollections.observableArrayList());

    @XmlElement(name = "alias")
    public List<Alias> getAliases() {
        return aliases.get();
    }

    public SimpleListProperty<Alias> aliasesProperty() {
        return aliases;
    }

    public void setAliases(ObservableList<Alias> aliases) {
        this.aliases.set(aliases);
    }

    public void setAliases(List<Alias> aliases) {
        this.aliases = new SimpleListProperty<>(FXCollections.observableArrayList(aliases));
    }

    @Override
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public static AliasConfiguration read(InputStream is) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(AliasConfiguration.class, Alias.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        AliasConfiguration p = (AliasConfiguration) unmarshaller.unmarshal(is);
        return p;
    }

    @Override
    public String toString() {
        return "AliasConfiguration{" +
                "name='" + name.get() + '\'' +
                ", description='" + description.get() + '\'' +
                '}';
    }
}
