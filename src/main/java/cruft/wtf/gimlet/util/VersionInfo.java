package cruft.wtf.gimlet.util;

import cruft.wtf.gimlet.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This class is responsible for reading version information from the proper MANIFEST.MF file
 * in the classpath. In actuality, it just iterates over all found manifest files and extracts
 * the values specific for Gimlet.
 * <p>
 * The class is instantiated only once as a singleton. The version information is therefore
 * only attempted to load once.
 */
public final class VersionInfo {

    private static Logger logger = LoggerFactory.getLogger(VersionInfo.class);

    private String buildTimestamp;

    private String version;

    /**
     * Creates the VersionInfo.
     */
    private static VersionInfo versionInfo = new VersionInfo();

    private VersionInfo() {
        Enumeration<URL> all;
        try {
            all = Utils.class.getClassLoader().getResources(JarFile.MANIFEST_NAME);
        } catch (IOException e) {
            logger.error("Unable to load an enumeration of manifest files from the classpath", e);
            version = "Unknown (error while enumerating classpath)";
            buildTimestamp = "Unknown timestamp";
            return;
        }

        // Start by setting unknown information first.
        version = "Unknown version";
        buildTimestamp = "Unknown timestamp";

        while (all.hasMoreElements()) {
            URL next = all.nextElement();
            InputStream is;
            try {
                is = next.openStream();
                Manifest mf = new Manifest(is);
                is.close();
                Attributes attrs = mf.getMainAttributes();
                String v = attrs.getValue("Gimlet-Version");
                String bt = attrs.getValue("Gimlet-Build-Timestamp");
                if (v != null) {
                    version = v;
                }
                if (bt != null) {
                    buildTimestamp = bt;
                }
            } catch (IOException e) {
                logger.error("Unable to open JAR file from classpath for manifest information: '{}'. Skipping...", next);
            }
        }
    }

    public static String getBuildTimestamp() {
        return versionInfo.buildTimestamp;
    }

    public static String getVersion() {
        return versionInfo.version;
    }

    /**
     * Gets a version string which can be used throughout the application.
     *
     * @return The version String.
     */
    public static String getVersionString() {
        return "Gimlet (" + VersionInfo.getVersion() + ")";
    }
}
