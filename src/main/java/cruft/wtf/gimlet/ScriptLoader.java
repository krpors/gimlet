package cruft.wtf.gimlet;

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

/**
 * This class is responsible for loading Nashorn scripts from the filesystem.
 */
public final class ScriptLoader {

    private static final Logger log = LoggerFactory.getLogger(ScriptLoader.class);

    /**
     * Attempts to load scripts from the filesystem located in the given {@code baseDir}.
     * If some files fail to be parsed by the script engine, the Script is returned in the
     * list as well, except the {@link Script#valid} flag is set to false and the
     * {@link Script#error} attribute contains the actual error.
     *
     * @param baseDir   The base directory to search Nashorn compatible Javascript files..
     * @param createDir true if the given directory should be created.
     * @return The list of scripts.
     * @throws IOException When an exception occurs while reading files from disk.
     */
    public static List<Script> load(String baseDir, boolean createDir) throws IOException {
        log.info("Loading scripts from {}", baseDir);
        Path p = Paths.get(baseDir);

        if (createDir) {
            Files.createDirectories(p);
        }

        List<Script> list = new ArrayList<>();

        Files.walk(p)
                .map(Path::toFile)
                .filter(File::isFile)
                .forEach(file -> {
                    Script s = new Script();
                    try {
                        s = fromFile(file);
                    } catch (ScriptException e) {
                        log.warn(String.format("Unable to parse script '%s'", file), e);
                        s.setError(e.getMessage());
                    } catch (IOException e) {
                        log.warn(String.format("Unable to open script file '%s'", file), e);
                        s.setError(e.getMessage());
                    } catch (Exception e) {
                        log.warn(String.format("Unexpected error while parsing script '%s'", file), e);
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
        ScriptEngine engine = mgr.getEngineByName("beanshell");

        // Auto-closeable. If anything fails, exception propagates up the stack.
        try (FileReader fr = new FileReader(file)) {
            engine.eval(fr);
        }

        Object o = engine.eval(Script.SCRIPT_REGISTER_FUNCTION);
        if (o instanceof String[]) {
            String[] strings = (String[]) o;
            if (strings.length != 3) {
                throw new ScriptException("The "
                        + Script.SCRIPT_REGISTER_FUNCTION
                        + " function should return a string array with 3 elements");
            }

            String name = strings[0];
            String desc = strings[1];
            String auth = strings[2];

            if (name == null || desc == null || auth == null) {
                // TODO: something more descriptive.
                throw new ScriptException("The " + Script.SCRIPT_REGISTER_FUNCTION + " function did return null values");
            }

            s.setName(name);
            s.setDescription(desc);
            s.setAuthor(auth);

        } else {
            throw new ScriptException("The " + Script.SCRIPT_REGISTER_FUNCTION + " function must return something useful");
        }

        s.setEngine(engine);

        return s;
    }
}
