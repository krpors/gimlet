package cruft.wtf.gimlet;

import jdk.nashorn.api.scripting.JSObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A {@link Script} contains the fields, methods and logic which are necessary to create a Gimlet compatible script.
 */
public class Script {

    private String name;

    private String description;

    private String author;

    private ScriptEngine engine;

    private static final String SCRIPT_REGISTER_FUNCTION = "script_register()";

    private static final String SCRIPT_EXECUTE_FUNCTION = "script_execute()";


    /**
     * Creates a {@link Script} from the given file.
     *
     * @param file The file to open.
     * @return The {@link Script} object, which can also be used to execute the Script.
     * @throws ScriptException When something fails.
     */
    public static Script fromFile(final File file) throws ScriptException, IOException {
        Script s = new Script();

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("nashorn");

        // Auto-closeable. If anything fails, exception propagates up the stack.
        try (FileReader fr = new FileReader(file)) {
            engine.eval(fr);
        }

        Object o = engine.eval(SCRIPT_REGISTER_FUNCTION);
        if (o instanceof JSObject) {
            JSObject json = (JSObject) o;

            Object name = json.getMember("name");
            Object desc = json.getMember("description");
            Object auth = json.getMember("author");

            if (name == null || desc == null || auth == null) {
                // TODO: something more descriptive.
                throw new ScriptException("The " + SCRIPT_REGISTER_FUNCTION + " function did not return the necessary JSON object.");
            }

            s.setName(String.valueOf(json.getMember("name")));
            s.setDescription(String.valueOf(json.getMember("description")));
            s.setAuthor(String.valueOf(json.getMember("author")));
        } else {
            throw new ScriptException("The " + SCRIPT_REGISTER_FUNCTION + " function must return something useful");
        }

        s.setEngine(engine);

        return s;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    private void setEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public void put(String key, Object binding) {
        this.engine.put(key, binding);
    }

    public Object execute() throws ScriptException {
        return this.engine.eval(SCRIPT_EXECUTE_FUNCTION);
    }
}
