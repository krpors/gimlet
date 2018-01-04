package cruft.wtf.gimlet;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;

/**
 * A {@link TimedTask} is just a utility task which can be used to measure the time spent in the implementation of this
 * task by using the {@link #start} and {@link #stop} methods.
 *
 * @param <V> The bleh.
 */
public abstract class TimedTask<V> extends Task<V> {

    private LongProperty time = new SimpleLongProperty(-1);

    private Long start;

    public long getTime() {
        return time.get();
    }

    /**
     * The property which can be used to listen on etc.
     *
     * @return The {@link LongProperty} containing the spent time.
     */
    public LongProperty timeProperty() {
        return time;
    }

    /**
     * Simply sets the start time to right now.
     */
    protected void start() {
        start = System.currentTimeMillis();
    }

    /**
     * Sets the time property to the current time, minus the start time,
     */
    protected void stop() {
        time.set(System.currentTimeMillis() - start);
    }
}
