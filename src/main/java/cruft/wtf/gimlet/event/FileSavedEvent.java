package cruft.wtf.gimlet.event;

import java.io.File;

public class FileSavedEvent {
    private File file;

    public FileSavedEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
