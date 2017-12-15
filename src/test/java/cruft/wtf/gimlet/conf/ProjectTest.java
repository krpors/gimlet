package cruft.wtf.gimlet.conf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProjectTest {
    @Test
    public void read() throws Exception {
        Project p = Project.read(ProjectTest.class.getResourceAsStream("/project.xml"));

        assertNotNull(p.getAliases());
        assertEquals(2, p.getAliases().size());
    }

}