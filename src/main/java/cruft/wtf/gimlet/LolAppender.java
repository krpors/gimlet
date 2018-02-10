package cruft.wtf.gimlet;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import cruft.wtf.gimlet.ui.LogTable;

public class LolAppender extends AppenderBase<ILoggingEvent> {

    public static LogTable table;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (table != null) {
            table.getItems().add(iLoggingEvent.getFormattedMessage());
            table.scrollToEnd();
        }
    }
}
