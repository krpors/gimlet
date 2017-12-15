package cruft.wtf.gimlet.conf;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class Query {

    private String name;

    private String description;

    private String content;

    private List<String> columnSelectors;

    private List<Query> subQueries;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @XmlElementWrapper(name = "column-selectors")
    @XmlElement(name = "column")
    public List<String> getColumnSelectors() {
        return columnSelectors;
    }

    public void setColumnSelectors(List<String> columnSelectors) {
        this.columnSelectors = columnSelectors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "query")
    public List<Query> getSubQueries() {
        return subQueries;
    }

    public void setSubQueries(List<Query> subQueries) {
        this.subQueries = subQueries;
    }
}
