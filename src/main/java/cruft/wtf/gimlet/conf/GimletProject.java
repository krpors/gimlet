package cruft.wtf.gimlet.conf;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@XmlRootElement(name = "gimlet-project")
@XmlType(propOrder = {
        "name",
        "description",
        "aliases",
        "queries"
})
public class GimletProject {

    private StringProperty name = new SimpleStringProperty("");

    private StringProperty description = new SimpleStringProperty("");

    private SimpleListProperty<Alias> aliases = new SimpleListProperty<>(FXCollections.observableArrayList());

    private SimpleListProperty<Query> queries = new SimpleListProperty<>(FXCollections.observableArrayList());

    /**
     * The filename which was used to unmarshal from. Can be null if this is a clean project.
     */
    private File filename;

    @XmlElementWrapper(name = "aliases")
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

    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "query")
    public List<Query> getQueries() {
        return queries.get();
    }

    public SimpleListProperty<Query> queriesProperty() {
        return queries;
    }

    public void setQueries(ObservableList<Query> queries) {
        this.queries.set(queries);
    }

    @XmlTransient
    public File getFile() {
        return filename;
    }

    public void setFile(File file) {
        this.filename = file;
    }

    /**
     * Traverses all queries defined in the project, in search for the one which has the specified name.
     * @param name
     * @return
     */
    public Query findQueryByName(final String name) {
        Deque<Query> queue = new ArrayDeque<>(getQueries());
        while (!queue.isEmpty()) {
            Query next = queue.pop();
            if (name.equals(next.getName())) {
                return next;
            }

            queue.addAll(next.getSubQueries());
        }
        return null;
    }

    /**
     * Reads from an {@link InputStream} and returns an unmarshalled {@link GimletProject}.
     *
     * @param is The {@link InputStream}.
     * @return The {@link GimletProject}.
     * @throws JAXBException When unmarshalling failed.
     */
    public static GimletProject read(InputStream is) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(GimletProject.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        return (GimletProject) unmarshaller.unmarshal(is);
    }

    public static GimletProject read(final File file) throws JAXBException, FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file);
        }
        JAXBContext ctx = JAXBContext.newInstance(GimletProject.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        GimletProject gp = (GimletProject) unmarshaller.unmarshal(file);
        gp.setFile(file);
        return gp;
    }

    public void writeToFile() throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(GimletProject.class);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(this, getFile());
    }

    @Override
    public String toString() {
        return "GimletProject{" +
                "name=" + name.get() +
                ", filename=" + filename +
                '}';
    }
}
