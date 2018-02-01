package cruft.wtf.gimlet;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Application-wide configuration class. This class is responsible for reading and writing configuration properties
 * from and to the Gimlet config file, which resides in the user's {@code home directory/.gimlet/config}.
 */
public final class Configuration extends Properties {

    private static Logger logger = LoggerFactory.getLogger(Configuration.class);

    /**
     * The one and only instance.
     */
    private static Configuration instance = new Configuration();

    private static final File CONFIG_FILE = new File(System.getProperty("user.home") + "/.gimlet/", "config");

    private Configuration() {
    }

    public static Configuration getInstance() {
        return instance;
    }

    public File getConfigFile() {
        return CONFIG_FILE;
    }

    /**
     * Gets an {@link Optional} integer property from the map of properties.
     *
     * @param key The key to look up.
     * @return {@link Optional#empty()} when the key cannot be found or parsed as an integer.
     */
    public Optional<Integer> getIntegerProperty(Key key) {
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

    /**
     * Gets an {@link Optional} boolean property from the map of properties.
     *
     * @param key The key to look up.
     * @return {@link Optional#empty()} when the key cannot be found.
     */
    public Optional<Boolean> getBooleanProperty(Key key) {
        String property = getProperty(key);
        if (property == null) {
            return Optional.empty();
        }

        return Optional.of(Boolean.valueOf(property));
    }

    /**
     * Gets an {@link Optional} double property from the map of properties.
     *
     * @param key The key to look up.
     * @return {@link Optional#empty()} when the key cannot be found or parsed as a double.
     */
    public Optional<Double> getDoubleProperty(Key key) {
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

    /**
     * Gets a property as a String, or if it can't be found, return an empty {@link Optional}.
     *
     * @param key The key to look up.
     * @return {@link Optional#empty()} when the key cannot be found.
     */
    public Optional<String> getStringProperty(Key key) {
        String property = getProperty(key);
        if (property == null) {
            return Optional.empty();
        }

        return Optional.of(property);
    }

    public String getProperty(Key key) {
        return getProperty(key.getName());
    }

    public void setProperty(Key key, Object value) {
        setProperty(key.getName(), value.toString());
    }

    /**
     * Loads the property file given by {@link Configuration#CONFIG_FILE}.
     *
     * @throws IOException When the file cannot be read.
     */
    public void load() throws IOException {
        if (!CONFIG_FILE.exists()) {
            logger.info("Configuration file '{}' does not exist; setting default values", CONFIG_FILE);
            return;
        }
        FileInputStream fis = new FileInputStream(CONFIG_FILE);
        load(fis);
        fis.close();
    }

    /**
     * Writes the property file given by {@link Configuration#CONFIG_FILE}. Parent directory will be created.
     *
     * @throws IOException When the file could not be written.
     */
    public void write() throws IOException {
        Files.createParentDirs(CONFIG_FILE);
        FileOutputStream fos = new FileOutputStream(CONFIG_FILE);
        store(fos, "Gimlet config");
        logger.info("Written configuration file '{}'", CONFIG_FILE);
        fos.close();
    }

    /**
     * This enumeration contains all possible configuration keys.
     */
    public enum Key {
        LAST_PROJECT_FILE,
        TRUNCATE_SIZE,
        WINDOW_MAXIMIZED,;

        public String getName() {
            return name().toLowerCase().replace('_', '.');
        }
    }
}
