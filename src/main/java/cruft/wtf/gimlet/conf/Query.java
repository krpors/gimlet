package cruft.wtf.gimlet.conf;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(propOrder = {
        "name",
        "description",
        "columnSelectors",
        "content",
        "subQueries"
})
public class Query {

    private StringProperty name = new SimpleStringProperty();

    private StringProperty description = new SimpleStringProperty();

    private StringProperty content = new SimpleStringProperty();

    private SimpleListProperty<String> columnSelectors = new SimpleListProperty<>(FXCollections.observableArrayList());

    private SimpleListProperty<Query> subQueries  = new SimpleListProperty<>(FXCollections.observableArrayList());

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
        return columnSelectors.get();
    }

    public SimpleListProperty<String> columnSelectorsProperty() {
        return columnSelectors;
    }

    public void setColumnSelectors(List<String> columnSelectors) {
        this.columnSelectors.setAll(columnSelectors);
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
    public List<Query> getSubQueries() {
        return subQueries.get();
    }

    public SimpleListProperty<Query> subQueriesProperty() {
        return subQueries;
    }

    public void setSubQueries(List<Query> subQueries) {
        this.subQueries.setAll(subQueries);
    }

    @Override
    public String toString() {
        return "Query{name='" + name + "}";
    }
}
