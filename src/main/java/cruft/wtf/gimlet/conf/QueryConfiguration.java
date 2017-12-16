package cruft.wtf.gimlet.conf;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.List;

@XmlRootElement(name = "queries")
public class QueryConfiguration {

    private List<Query> queries;

    @XmlElement(name = "query")
    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public static QueryConfiguration read(InputStream is) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(QueryConfiguration.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        QueryConfiguration p = (QueryConfiguration) unmarshaller.unmarshal(is);
        return p;
    }
}
