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

    ACCOUNT_LOGIN("/icons/lan-connect-24px.png", true),
    ACCOUNT_LOGOUT("/icons/logout-variant-24px.png", true),
    ARROW_DOWN("/icons/arrow-down-24px.png", true),
    ARROW_UP("/icons/arrow-up-24px.png", true),
    CANCEL("/icons/cancel-24px.png", true),
    CLOCK("/icons/clock-24px.png", true),
    CODE("/icons/code-not-equal-24px.png", true),
    COG("/icons/settings-24px.png", true),
    COPY("/icons/content-copy-24px.png", true),
    CUT("/icons/content-cut-24px.png", true),
    DOCUMENT("/icons/database-24px.png", true),
    FOLDER("/icons/clock-24px.png", true),
    MAGNIFYING_GLASS("/icons/database-search-24px.png", true),
    PASTE("/icons/content-paste-24px.png", true),
    PENCIL("/icons/pencil-24px.png", true),
    PERSON("/icons/account-24px.png", true),
    PLUS("/icons/plus-24px.png", true),
    RUN("/icons/run-24px.png", true),
    SAVE("/icons/content-save-24px.png", true),
    SKULL("/icons/skull-24px.png", true),
    SPREADSHEET("/icons/table-large-24px.png", true),
    TABLE_COLUMN_WIDTH("/icons/table-column-width-24px.png", true),
    TRASH("/icons/delete-forever-24px.png", true),
    WARNING("/icons/alert-outline-24px.png", true),

    /**
     * Other, larger icons.
     */
    DIALOG_ERROR("/icons/dialog-error.png", false),
    LOCK_LOCKED_4X("/icons/lock-48px.png", false);

    private String  path;
    private boolean resize;

    private static Logger logger = LoggerFactory.getLogger(Images.class);

    Images(String path, boolean resize) {
        this.path = path;
        this.resize = resize;
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
        ImageView view = new ImageView(imageCache.get(this));
        if (resize) {
            view.setFitHeight(16);
            view.setPreserveRatio(true);
        }
        return view;
    }

}
