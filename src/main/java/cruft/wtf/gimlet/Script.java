package cruft.wtf.gimlet;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;

/**
 * A {@link Script} contains the fields, methods and logic which are necessary to create a Gimlet compatible script.
 */
public final class Script implements Comparable<Script> {

    private String name;

    private String description;

    private String author;

    private File file;

    private boolean valid;

    private String error;

    private ScriptEngine engine;

    public static final String SCRIPT_REGISTER_FUNCTION = "script_register()";

    public static final String SCRIPT_EXECUTE_FUNCTION = "script_execute()";

    public Script() {
        this.valid = true;
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

    public void setEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.valid = false;
        this.error = error;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void put(String key, Object binding) {
        this.engine.put(key, binding);
    }

    public Object execute() throws ScriptException {
        return this.engine.eval(SCRIPT_EXECUTE_FUNCTION);
    }

    @Override
    public String toString() {
        return "Script{" +
                "name='" + name + '\'' +
                ", file=" + file +
                ", valid=" + valid +
                '}';
    }

    @Override
    public int compareTo(Script o) {
        if (o == null) {
            return 1;
        }

        if (o.getName() == null || this.getName() == null) {
            return -1;
        }

        return this.getName().compareTo(o.getName());
    }
}
