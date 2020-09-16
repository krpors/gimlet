package cruft.wtf.gimlet;

import com.google.common.io.Files;
import cruft.wtf.gimlet.util.Xdg;
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
 * from and to the Gimlet config file. The configuration file's location depends on the
 * <a href="https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html">XDG directory specification</a>,
 * specifically the {@code XDG_CONFIG_HOME} environment variable. If no such environment variable is set, the default
 * will be used of {@code $HOME/.confg/gimlet} containing the {@code config} file.
 */
public final class Configuration extends Properties {

    private static Logger logger = LoggerFactory.getLogger(Configuration.class);

    /**
     * The one and only instance.
     */
    private static Configuration instance = new Configuration();

    /**
     * Will be initialized in the constructor, based on the {@code XDG_CONFIG_HOME}.
     */
    private static File configFile;

    private Configuration() {
        configFile = Xdg.getConfigHome().resolve("config").toFile();
        logger.debug("Gimlet configuration file is '{}'", configFile);
    }

    public static File getConfigFile() {
        return configFile;
    }

    /**
     * Gets an {@link Optional} integer property from the map of properties.
     *
     * @param key The key to look up.
     * @return {@link Optional#empty()} when the key cannot be found or parsed as an integer.
     */
    public static Optional<Integer> getIntegerProperty(Key key) {
        String property = instance.getProperty(key);
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
    public static Optional<Boolean> getBooleanProperty(Key key) {
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
    public static Optional<Double> getDoubleProperty(Key key) {
        String property = instance.getProperty(key);
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
    public static Optional<String> getStringProperty(Key key) {
        String property = instance.getProperty(key);
        if (property == null) {
            return Optional.empty();
        }

        return Optional.of(property);
    }

    private static String getProperty(Key key) {
        return instance.getProperty(key.getName());
    }

    public static void setProperty(Key key, Object value) {
        instance.setProperty(key.getName(), value.toString());
    }

    /**
     * Sets the default values for the configuration.
     */
    private static void setDefaults() {
        setProperty(Key.TRUNCATE_SIZE, 32);
        setProperty(Key.WINDOW_MAXIMIZED, false);
        setProperty(Key.ENABLE_DARK_THEME, false);
    }

    /**
     * Loads the property file given by {@link Configuration#configFile}.
     *
     * @throws IOException When the file cannot be read.
     */
    public static void load() throws IOException {
        if (!configFile.exists()) {
            logger.info("Configuration file '{}' does not exist; setting default values", configFile);
            setDefaults();
            return;
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            instance.load(fis);
        }
    }

    /**
     * Writes the property file given by {@link Configuration#configFile}. Parent directory will be created.
     *
     * @throws IOException When the file could not be written.
     */
    public static void write() throws IOException {
        Files.createParentDirs(configFile);
        FileOutputStream fos = new FileOutputStream(configFile);
        instance.store(fos, "Gimlet config");
        logger.info("Written configuration file '{}'", configFile);
        fos.close();
    }

    /**
     * Removes a key by enum.
     *
     * @param key The key to remove.
     */
    public static void remove(Key key) {
        instance.remove(key.getName());
    }

    /**
     * This enumeration contains all possible configuration keys.
     */
    public enum Key {
        CONFIRM_APPLICATION_EXIT,
        LAST_PROJECT_FILE,
        SAVE_ON_EXIT,
        TRUNCATE_SIZE,
        WINDOW_MAXIMIZED,
        ENABLE_DARK_THEME,
        ;

        public String getName() {
            return name().toLowerCase().replace('_', '.');
        }
    }
}
