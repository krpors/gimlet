package cruft.wtf.gimlet;

import org.junit.Test;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ScriptTest {

    @Test
    public void okScript() throws IOException, ScriptException {
        URL url = ScriptTest.class.getResource("/scripts/ok.js");
        File file = new File(url.getFile());
        Script s = Script.fromFile(file);
        assertEquals("Example script", s.getName());
        assertEquals("Example script description", s.getDescription());
        assertEquals("Herp A. Derp", s.getAuthor());
        Object o = s.execute();
        assertNotNull(o);
    }

    @Test
    public void badRegisterFunction() throws IOException {
        URL url = ScriptTest.class.getResource("/scripts/bad_register.js");
        File file = new File(url.getFile());
        try {
            Script.fromFile(file);
            fail("Expected exception at this point");
        } catch (ScriptException e) {
            // OK
        }
    }

    @Test
    public void okBindings() throws IOException, ScriptException {
        URL url = ScriptTest.class.getResource("/scripts/ok_bindings.js");
        File file = new File(url.getFile());
        Script s = Script.fromFile(file);

        s.put("someVar", 123);

        Object o = s.execute(); // will execute 123 * 8
        assertEquals(984.0, o);
    }
}
