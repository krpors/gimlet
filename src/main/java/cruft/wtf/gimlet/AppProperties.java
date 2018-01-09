package cruft.wtf.gimlet;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class AppProperties extends Properties {

    private static Logger logger = LoggerFactory.getLogger(AppProperties.class);

    public static AppProperties instance = new AppProperties();

    private static final File CONFIG_FILE = new File(System.getProperty("user.home") + "/.gimlet/", "config");

    private AppProperties() {
    }

    public File getConfigFile() {
        return CONFIG_FILE;
    }

    public int getInteger(Keys key, int def) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public Optional<Integer> getIntegerProperty(Keys key) {
        String property = getProperty(key);
        if (property == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(property));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> getBooleanProperty(Keys key) {
        String property = getProperty(key);
        if (property == null) {
            return Optional.empty();
        }

        return Optional.of(Boolean.valueOf(property));
    }

    public Optional<Double> getDoubleProperty(Keys key) {
        String property = getProperty(key);
        if (property == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(property));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public String getProperty(Keys key) {
        return getProperty(key.getPropertyName());
    }

    public void setProperty(Keys key, Object value) {
        setProperty(key.getPropertyName(), value.toString());
    }

    public void load() throws IOException {
        if (!CONFIG_FILE.exists()) {
            logger.info("Configuration file '{}' does not exist; setting default values");
            return;
        }
        FileInputStream fis = new FileInputStream(CONFIG_FILE);
        load(fis);
        fis.close();
    }

    public void write() throws IOException {
        Files.createParentDirs(CONFIG_FILE);
        FileOutputStream fos = new FileOutputStream(CONFIG_FILE);
        store(fos, "Gimlet config");
        logger.info("Written configuration file '{}'", CONFIG_FILE);
        fos.close();
    }

    public enum Keys {
        LAST_PROJECT_FILE("last.project.file", ""),
        WINDOW_WIDTH("window.width", "800"),
        WINDOW_HEIGHT("window.height", "600"),
        WINDOW_MAXIMIZED("window.maximized", "false"),
        QUERY_TREE_DIVIDER_POSITION("query.tree.divider.pos", "0.5");

        private String propertyName;

        private String def;

        Keys(String propertyName, String def) {
            this.propertyName = propertyName;
            this.def = def;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getDefault() {
            return def;
        }
    }
}
