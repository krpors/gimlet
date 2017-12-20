package cruft.wtf.gimlet.event;

import cruft.wtf.gimlet.conf.Alias;

/**
 * Emitted when a connection attempt has occurred.
 */
public class ConnectEvent {
    private Alias alias;

    public ConnectEvent(Alias alias) {
        this.alias = alias;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }
}
