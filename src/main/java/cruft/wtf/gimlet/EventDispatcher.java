package cruft.wtf.gimlet;

import com.google.common.eventbus.EventBus;

/**
 * JVM-wide event dispatcher.
 */
public final class EventDispatcher {
    private static EventDispatcher ourInstance = new EventDispatcher();

    private EventBus eventBus;

    public static EventDispatcher getInstance() {
        return ourInstance;
    }

    private EventDispatcher() {
        this.eventBus = new EventBus("javafx-dispatcher");
    }

    public String identifier() {
        return eventBus.identifier();
    }

    public void register(Object object) {
        eventBus.register(object);
    }

    public void unregister(Object object) {
        eventBus.unregister(object);
    }

    public void post(Object event) {
        eventBus.post(event);
    }
}
