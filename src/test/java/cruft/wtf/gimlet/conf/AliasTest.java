package cruft.wtf.gimlet.conf;

import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class AliasTest {

    @Test
    public void asd() throws JAXBException {
        Alias a = new Alias();
        a.getJdbcProperties().put("k1", "v1");

        JAXBContext ctx = JAXBContext.newInstance(Alias.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(a, System.out);
    }
}
