package cruft.wtf.gimlet;

import java.io.IOException;
import java.io.InputStream;

public final class TestUtils {

    public static String readFromClasspath(String name) {
        StringBuilder b = new StringBuilder();
        try (InputStream is = DataExporterTest.class.getResourceAsStream(name)) {
            int c;
            while ((c = is.read()) != -1) {
                b.append((char)c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return b.toString();
    }
}
