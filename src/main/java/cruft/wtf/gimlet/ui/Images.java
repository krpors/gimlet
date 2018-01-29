package cruft.wtf.gimlet.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Sort of factory enumeration, to create {@link ImageView} objects on the fly. Scenes in JavaFX cannot contain the
 * same Node twice, so we have to create them on the fly as needed, via the {@link #imageView()} method.
 */
public enum Images {

    ACCOUNT_LOGIN("/icons/account-login-2x.png"),
    ACCOUNT_LOGOUT("/icons/account-logout-2x.png"),
    BOLT("/icons/bolt-2x.png"),
    CLOCK("/icons/clock-2x.png"),
    CODE("/icons/code-2x.png"),
    COG("/icons/cog-2x.png"),
    DASHBOARD("/icons/dashboard-2x.png"),
    DIALOG_ERROR("/icons/dialog-error.png"),
    DOCUMENT("/icons/document-2x.png"),
    ELLIPSES("/icons/ellipses-2x.png"),
    FILE("/icons/file-2x.png"),
    FOLDER("/icons/folder-2x.png"),
    LOCK_LOCKED_4X("/icons/lock-locked-4x.png"),
    MAGNIFYING_GLASS("/icons/magnifying-glass-2x.png"),
    MEDIA_PLAY("/icons/media-play-2x.png"),
    MINUS("/icons/minus-2x.png"),
    PENCIL("/icons/pencil-2x.png"),
    PERSON("/icons/person-2x.png"),
    PLUS("/icons/plus-2x.png"),
    POWER_STANDBY("/icons/power-standby-2x.png"),
    PULSE("/icons/pulse-2x.png"),
    QUESTION_MARK("/icons/question-mark-2x.png"),
    RELOAD("/icons/reload-2x.png"),
    SPREADSHEET("/icons/spreadsheet-2x.png"),
    TRASH("/icons/trash-2x.png"),
    WARNING("/icons/warning-2x.png"),
    WRENCH("/icons/wrench-2x.png"),;

    private String path;

    private static Logger logger = LoggerFactory.getLogger(Images.class);

    Images(String path) {
        this.path = path;
    }

    private static Map<Images, Image> imageCache;

    static {
        imageCache = new HashMap<>();
        for (Images img : Images.values()) {
            InputStream is = Images.class.getResourceAsStream(img.path);
            if (is == null) {
                logger.error("Unable to find image file {}", img.path);
                continue;
            }
            Image image = new Image(Images.class.getResourceAsStream(img.path));
            imageCache.put(img, image);
        }
    }

    public ImageView imageView() {
        return new ImageView(imageCache.get(this));
    }

}
