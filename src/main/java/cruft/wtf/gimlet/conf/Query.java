package cruft.wtf.gimlet.conf;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@XmlType(propOrder = {
        "content",
        "subQueries",
        "referencedQueries"
})
@XmlSeeAlso({Item.class})
public class Query extends Item {

    private StringProperty content = new SimpleStringProperty();

    private SimpleListProperty<Query> subQueries = new SimpleListProperty<>(FXCollections.observableArrayList());

    private SimpleListProperty<String> referencedQueries = new SimpleListProperty<>(FXCollections.observableArrayList());

    private Query parentQuery;

    private GimletProject parentProject;

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

    public List<Query> findReferencesQueries() {
        GimletProject parent = findGimletProject();
        return null;
    }

    @XmlElementWrapper(name = "referencedQueries")
    @XmlElement(name = "query")
    public ObservableList<String> getReferencedQueries() {
        return referencedQueries.get();
    }

    public SimpleListProperty<String> referencedQueriesProperty() {
        return referencedQueries;
    }

    public void setReferencedQueries(ObservableList<String> referencedQueries) {
        this.referencedQueries.set(referencedQueries);
    }

    /**
     * Package protected. Only used for testing.
     *
     * @return The parent project attribute.
     */
    @XmlTransient
    GimletProject getParentProject() {
        return parentProject;
    }

    void setParentProject(GimletProject parentProject) {
        this.parentProject = parentProject;
    }

    @XmlTransient
    public Query getParentQuery() {
        return parentQuery;
    }

    /**
     * This method will find the GimletProject parent for this query. If the query is a (deeply)
     * nested query, it will traverse the parent queries until the parent project is found.
     *
     * @return The Gimlet parent project.
     */
    public GimletProject findGimletProject() {
        // If this query is a 'root' query of the project, the parentProject attribute
        // has a non-null value. In that case we can immediately return the parent project.
        if (parentProject != null) {
            return parentProject;
        }

        // In other cases, this query is a subquery of another parent query.
        // So we will attempt to traverse up to the parent(s) until we find a
        // non-null parent gimlet project.
        Query q = getParentQuery();
        while (q != null) {
            if (q.getParentProject() != null) {
                return q.getParentProject();
            }

            q = q.getParentQuery();

        }

        // This is one of those famous cases of "this should not happen". When a
        // gimlet project is loaded properly, there should always be a parent project
        // for a query.
        return null;
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

        if (parent instanceof GimletProject) {
            GimletProject gimletProject = (GimletProject) parent;
            this.parentProject = gimletProject;
            System.out.println("parent project is " + gimletProject);
        }
    }

    @Override
    public String toString() {
        return "Query{name='" + nameProperty().get() + "}";
    }
}
