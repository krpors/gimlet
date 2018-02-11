package cruft.wtf.gimlet;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import cruft.wtf.gimlet.event.EventDispatcher;

/**
 * Extremely simple appender which effectively just dispatches the logging event to the event bus.
 *
 * @see cruft.wtf.gimlet.ui.LogTable#onLoggingEvent(ILoggingEvent)
 */
public class DispatchAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        EventDispatcher.getInstance().post(iLoggingEvent);
    }
}
