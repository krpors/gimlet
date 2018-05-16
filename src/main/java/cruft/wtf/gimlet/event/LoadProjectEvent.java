package cruft.wtf.gimlet.event;

import java.io.File;

/**
 * Event emitted when a project is attempt to be loaded.
 */
public class LoadProjectEvent {
    private final File file;

    public LoadProjectEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
