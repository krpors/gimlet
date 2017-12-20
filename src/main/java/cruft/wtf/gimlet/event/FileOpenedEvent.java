package cruft.wtf.gimlet.event;

import cruft.wtf.gimlet.conf.GimletProject;

import java.io.File;

/**
 * Emitted when a file has been opened successfully.
 */
public class FileOpenedEvent {
    private File file;

    private GimletProject gimletProject;

    public FileOpenedEvent(File file, GimletProject gimletProject) {
        this.file = file;
        this.gimletProject = gimletProject;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public GimletProject getGimletProject() {
        return gimletProject;
    }

    public void setGimletProject(GimletProject gimletProject) {
        this.gimletProject = gimletProject;
    }
}
