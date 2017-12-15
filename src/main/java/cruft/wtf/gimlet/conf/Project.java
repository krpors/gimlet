package cruft.wtf.gimlet.conf;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.List;

@XmlRootElement(name = "project")
public class Project {

    private List<Alias> aliases;

    private List<QuerySet> querySet;

    @XmlElementWrapper(name = "aliases")
    @XmlElement(name = "alias")
    public List<Alias> getAliases() {
        return aliases;
    }

    public void setAliases(List<Alias> aliases) {
        this.aliases = aliases;
    }

    @XmlElementWrapper(name = "query-sets")
    @XmlElement(name = "query-set")
    public List<QuerySet> getQuerySet() {
        return querySet;
    }

    public void setQuerySet(List<QuerySet> querySet) {
        this.querySet = querySet;
    }

    public static Project read(InputStream is) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(Project.class, Alias.class, QuerySet.class, Query.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        Project p = (Project) unmarshaller.unmarshal(is);
        return p;
    }
}
