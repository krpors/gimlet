package cruft.wtf.gimlet.jdbc.task;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class's purpose is to periodically check validity of an SQL connection,
 * and report to the user whether it has been closed. A {@link Timer} is used
 * to schedule the check at a fixed rate.
 */
public class ConnectionValidityTimer {

    private Logger logger = LoggerFactory.getLogger(ConnectionValidityTimer.class);

    private Timer timer;

    private boolean scheduled = false;

    private Connection connection;

    private final int checkTimeout;

    private final String name;

    private EventHandler<ActionEvent> eventHandler;

    public ConnectionValidityTimer(final String name, int checkTimeout) {
        this.name = name;
        this.checkTimeout = checkTimeout;
        this.timer = new Timer(String.format("Connection validity timer for '%s'", name), true);
    }

    /**
     * Schedules the timer at a fixed rate.
     */
    public void schedule() {
        if (!scheduled) {
            timer.scheduleAtFixedRate(new CheckTask(), 0, 30000);
            scheduled = true;

            logger.debug("Scheduled connection validity task for '{}'", name);
        }
    }

    /**
     * Cancels the timer.
     */
    public void cancel() {
        timer.cancel();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Sets the callback handler when a disconnect happens.
     *
     * @param evt The event.
     */
    public void setOnDisconnect(EventHandler<ActionEvent> evt) {
        this.eventHandler = evt;
    }

    /**
     * The actual scheduled task to periodically check the validity of the SQL connection.
     */
    private class CheckTask extends TimerTask {

        @Override
        public void run() {
            try {
                if (!connection.isValid(checkTimeout)) {
                    // Connection deemed invalid. Cancel the timer, close the connection.
                    cancel();
                    Platform.runLater(() -> eventHandler.handle(new ActionEvent(this, null)));
                }
            } catch (SQLException e) {
                logger.error("The connection was closed", e);
            }
        }
    }
}
