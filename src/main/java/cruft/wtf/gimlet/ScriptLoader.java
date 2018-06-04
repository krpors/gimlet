package cruft.wtf.gimlet;

import jdk.nashorn.api.scripting.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ScriptLoader {

    private static final Logger log = LoggerFactory.getLogger(ScriptLoader.class);

    public static List<Script> load(String baseDir) throws IOException {
        log.info("Loading scripts from {}", baseDir);
        Path p = Paths.get(baseDir);

        List<Script> list = new ArrayList<>();

        Files.walk(p)
                .map(Path::toFile)
                .filter(File::isFile)
                .forEach(file -> {
                    Script s = new Script();
                    try {
                        s = fromFile(file);
                    } catch (ScriptException e) {
                        log.error("Unable to read script", e);
                        s.setError(e.getMessage());
                    } catch (IOException e) {
                        log.error("Unable open file", e);
                        s.setError(e.getMessage());
                    } finally {
                        list.add(s);
                    }
                });

        // Prematurely sort the list based on name.
        Collections.sort(list);

        return list;
    }

    /**
     * Creates a {@link Script} from the given file.
     *
     * @param file The file to open.
     * @return The {@link Script} object, which can also be used to execute the Script.
     * @throws ScriptException When something fails.
     */
    public static Script fromFile(final File file) throws ScriptException, IOException {
        Script s = new Script();
        s.setFile(file);

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("nashorn");

        // Auto-closeable. If anything fails, exception propagates up the stack.
        try (FileReader fr = new FileReader(file)) {
            engine.eval(fr);
        }

        Object o = engine.eval(Script.SCRIPT_REGISTER_FUNCTION);
        if (o instanceof JSObject) {
            JSObject json = (JSObject) o;

            Object name = json.getMember("name");
            Object desc = json.getMember("description");
            Object auth = json.getMember("author");

            if (name == null || desc == null || auth == null) {
                // TODO: something more descriptive.
                throw new ScriptException("The " + Script.SCRIPT_REGISTER_FUNCTION + " function did not return the necessary JSON object.");
            }

            s.setName(String.valueOf(json.getMember("name")));
            s.setDescription(String.valueOf(json.getMember("description")));
            s.setAuthor(String.valueOf(json.getMember("author")));
        } else {
            throw new ScriptException("The " + Script.SCRIPT_REGISTER_FUNCTION + " function must return something useful");
        }

        s.setEngine(engine);

        return s;
    }
}