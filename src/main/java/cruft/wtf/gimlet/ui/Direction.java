package cruft.wtf.gimlet.ui;

/**
 * Enumeration to clarify movement direction.
 */
public enum Direction {
    UP(-1),
    DOWN(1);

    protected final int dir;

    Direction(int dir) {
        this.dir = dir;
    }
}
