package cruft.wtf.gimlet.conf;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

public class Query implements Item {

    private StringProperty name = new SimpleStringProperty();

    private StringProperty description = new SimpleStringProperty();

    private StringProperty content = new SimpleStringProperty();

    private List<String> columnSelectors = new ArrayList<>();


    private List<Query> subQueries = new ArrayList<>();

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

    public String getContent() {
        return content.get();
    }

    public StringProperty contentProperty() {
        return content;
    }

    public void setContent(String content) {
        this.content.set(content);
    }

    @XmlElementWrapper(name = "column-selectors")
    @XmlElement(name = "column")
    public List<String> getColumnSelectors() {
        return columnSelectors;
    }

    public void setColumnSelectors(List<String> columnSelectors) {
        this.columnSelectors = columnSelectors;
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

    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "query")
    public List<Query> getSubQueries() {
        return subQueries;
    }

    public void setSubQueries(List<Query> subQueries) {
        this.subQueries = subQueries;
    }

    @Override
    public String toString() {
        return "Query{name='" + name + "}";
    }
}
