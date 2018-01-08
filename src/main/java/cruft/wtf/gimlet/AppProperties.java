package cruft.wtf.gimlet;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties extends Properties {

    public static String LAST_PROJECT_FILE = "last.project.file";

    public static AppProperties instance = new AppProperties();

    private static String HOME_DIR = System.getProperty("user.home");

    private AppProperties() {
    }

    public void load() {
        File file = new File(HOME_DIR + "/.gimlet/", "config");
        if (!file.exists()) {
            System.out.println("file does not exist yet");
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write() {
        File file = new File(HOME_DIR + "/.gimlet/", "config");
        try {
            Files.createParentDirs(file);
            store(new FileOutputStream(file), "Gimlet config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
