package cruft.wtf.gimlet.conf;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.junit.jupiter.api.Test;

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
