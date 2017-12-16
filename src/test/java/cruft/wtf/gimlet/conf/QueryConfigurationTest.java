package cruft.wtf.gimlet.conf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QueryConfigurationTest {
    @Test
    public void read() throws Exception {
        QueryConfiguration c = QueryConfiguration.read(QueryConfigurationTest.class.getResourceAsStream("/queries.xml"));

        assertNotNull(c.getQueries());
        assertEquals(1, c.getQueries().size());


        Query first = c.getQueries().get(0);
        assertEquals("Select all addresses.", first.getName());
        assertEquals("This query selects all the main addresses.", first.getDescription());
        assertEquals("select * from address", first.getContent());

        assertNotNull(first.getColumnSelectors());
        assertEquals(2, first.getColumnSelectors().size());

        assertEquals(2, first.getSubQueries().size());
    }

}