package cruft.wtf.gimlet.conf;

import org.junit.Test;

public class AliasConfigurationTest {
    @Test
    public void read() throws Exception {
        AliasConfiguration c = AliasConfiguration.read(QueryConfigurationTest.class.getResourceAsStream("/aliases.xml"));

        System.out.println(c);
        System.out.println(c.getAliases().size());
        System.out.println(c.getAliases().get(0));
        System.out.println(c.getAliases().get(1));
    }
}
