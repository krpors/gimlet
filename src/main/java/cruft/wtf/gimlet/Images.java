package cruft.wtf.gimlet;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    COG("/icons/cog-2x.png"),
    DASHBOARD("/icons/dashboard-2x.png"),
    DIALOG_ERROR("/icons/dialog-error.png"),
    DOCUMENT("/icons/document-2x.png"),
    FILE("/icons/file-2x.png"),
    FOLDER("/icons/folder-2x.png"),
    MINUS("/icons/minus-2x.png"),
    PENCIL("/icons/pencil-2x.png"),
    PLUS("/icons/plus-2x.png"),
    PULSE("/icons/pulse-2x.png"),
    TRASH("/icons/trash-2x.png");

    private String path;

    Images(String path) {
        this.path = path;
    }

    private static Map<Images, Image> imageCache;

    static {
        imageCache = new HashMap<>();
        for (Images img : Images.values()) {
            Image image = new Image(Images.class.getResourceAsStream(img.path));
            imageCache.put(img, image);
        }
    }

    public ImageView imageView() {
        return new ImageView(imageCache.get(this));
    }

}
