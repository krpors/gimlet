package cruft.wtf.gimlet.event;

import cruft.wtf.gimlet.conf.Alias;

/**
 * Emitted when a connection attempt has occurred.
 */
public class ConnectEvent {

    public enum Type {
        INITATED,
        CONNECTED,
        CLOSED;
    }

    private Alias alias;
    private Type  type;

    public ConnectEvent(Type type, Alias alias) {
        this.type = type;
        this.alias = alias;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
