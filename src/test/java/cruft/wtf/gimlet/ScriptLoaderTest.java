package cruft.wtf.gimlet;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptLoaderTest {

    /**
     * This test checks the {@link ScriptLoader#load(String, boolean)} method.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void loadScripts() throws Exception {
        Path p = Paths.get(ScriptLoaderTest.class.getResource("/scripts/").toURI());
        List<Script> scripts = ScriptLoader.load(p.toString(), false);
        scripts.forEach(System.out::println);

        assertEquals(3, scripts.size());

        assertNull(scripts.get(0).getName());
        assertFalse(scripts.get(0).isValid());
        assertEquals("The script_register() function should return a string array with 3 elements", scripts.get(0).getError());

        assertEquals("Example script", scripts.get(1).getName());
        assertTrue(scripts.get(1).isValid());
        assertNull(scripts.get(1).getError());

        assertEquals("Example script for bindings", scripts.get(2).getName());
        assertTrue(scripts.get(2).isValid());
        assertNull(scripts.get(2).getError());
    }
}
