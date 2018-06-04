package cruft.wtf.gimlet.event;

public class ScriptExecutedEvent {

    private Object message;

    public ScriptExecutedEvent(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
