package cruft.wtf.gimlet.conf;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@XmlType(propOrder = {
        "content",
        "subQueries"
})
@XmlSeeAlso({Item.class})
public class Query extends Item {

    private StringProperty content = new SimpleStringProperty();

    private SimpleListProperty<Query> subQueries = new SimpleListProperty<>(FXCollections.observableArrayList());

    private Query parentQuery;

    public Query() {
    }

    /**
     * Copy constructor to make a deep copy of the given query. The parent query is not copied and is set to null.
     *
     * @param other The query to make a copy of.
     */
    public Query(final Query other) {
        this.nameProperty().set(other.getName());
        this.descriptionProperty().set(other.getDescription());
        this.contentProperty().set(other.getContent());
        this.colorProperty().set(other.getColor());
        this.colorDisabledProperty().setValue(other.isColorDisabled());
        for (Query q : other.getSubQueries()) {
            Query copy = new Query(q);
            this.addSubQuery(copy);
        }
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

    /**
     * Adds a subquery to this query, and changes the parent immediately.
     *
     * @param query The query to add to this query.
     */
    public void addSubQuery(final Query query) {
        query.parentQuery = this;
        getSubQueries().add(query);
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

    /**
     * Gets the amount of descendants this query may have.
     *
     * @return The descendants count.
     */
    public int getDescendantCount() {
        Deque<Query> q = new ArrayDeque<>();
        q.add(this);
        int counter = -1;
        while (!q.isEmpty()) {
            Query next = q.pop();
            counter++;
            next.getSubQueries().forEach(q::push);
        }

        return counter;
    }

    @XmlTransient
    public Query getParentQuery() {
        return parentQuery;
    }

    /**
     * Copies values from another query, omitting the subqueries.
     *
     * @param other The other query.
     */
    public void copyFrom(final Query other) {
        setName(other.getName());
        setContent(other.getContent());
        setDescription(other.getDescription());
        setColor(other.getColor());
        setColorDisabled(other.isColorDisabled());
    }

    protected void afterUnmarshal(Unmarshaller um, Object parent) {
        if (parent instanceof Query) {
            Query query = (Query) parent;
            this.parentQuery = query;
        }
    }

    @Override
    public String toString() {
        return "Query{name='" + nameProperty().get() + "}";
    }
}
