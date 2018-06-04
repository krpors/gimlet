package cruft.wtf.gimlet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is a helper class for handling the
 * <a href="https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html">XDG
 * Base Directory specification</a>.
 */
public class Xdg {

    private static final Logger logger = LoggerFactory.getLogger(Xdg.class);

    /**
     * Returns the configuration base directory for Gimlet, based on the environment
     * variable {@code XDG_CONFIG_HOME}, or if null or empty, uses the default of {@code $HOME/.config/gimlet}.
     *
     * @return The {@link Path} for the configuration base of Gimlet.
     */
    public static Path getConfigHome() {
        // Used the XDG directory specification to check whether the env var is set. If so
        // then use that one, and if not, the default is used of $HOME/.config.
        String xdgHome = System.getenv("XDG_CONFIG_HOME");
        if (xdgHome == null || "".equals(xdgHome.trim())) {
            xdgHome = System.getProperty("user.home") + "/.config";
        } else {
            logger.debug("Using explicit XDG_CONFIG_HOME '{}'", xdgHome);
        }

        return Paths.get(xdgHome, "gimlet");
    }
}
