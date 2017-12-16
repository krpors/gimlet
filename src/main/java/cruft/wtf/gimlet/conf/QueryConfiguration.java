package cruft.wtf.gimlet.conf;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.List;

@XmlRootElement(name = "query-configuration")
public class QueryConfiguration implements Item {

    private String name;

    private String description;

    private List<Query> queries;

    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "query")
    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static QueryConfiguration read(InputStream is) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(QueryConfiguration.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        QueryConfiguration p = (QueryConfiguration) unmarshaller.unmarshal(is);
        return p;
    }
}
