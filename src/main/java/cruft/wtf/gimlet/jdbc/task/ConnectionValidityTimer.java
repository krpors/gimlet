package cruft.wtf.gimlet.jdbc.task;

import cruft.wtf.gimlet.Utils;
import cruft.wtf.gimlet.conf.Alias;
import cruft.wtf.gimlet.ui.ConnectionTab;
import cruft.wtf.gimlet.ui.Images;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
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

    private final int checkTimeout;

    private final ConnectionTab tab;

    public ConnectionValidityTimer(final ConnectionTab tab, int checkTimeout) {
        this.tab = Objects.requireNonNull(tab);
        this.checkTimeout = checkTimeout;

        this.timer = new Timer(String.format("Connection validity timer for '%s'", tab.getAlias().getName()), true);
    }

    /**
     * Schedules the timer at a fixed rate.
     */
    public void schedule() {
        if (!scheduled) {
            timer.scheduleAtFixedRate(new CheckTask(), 0, 60000);
            scheduled = true;

            logger.debug("Scheduled connection validity task for '{}'", tab.getAlias().getName());
        }
    }

    /**
     * Cancels the timer.
     */
    public void cancel() {
        timer.cancel();
    }

    /**
     * The actual scheduled task to periodically check the validity of the SQL connection.
     */
    private class CheckTask extends TimerTask {

        @Override
        public void run() {
            try {
                Connection connection = tab.getConnection();
                Alias alias = tab.getAlias();

                if (!connection.isValid(checkTimeout)) {
                    cancel();

                    // This task is run through a Timer, so use Platform.runLater.
                    Platform.runLater(() -> {
                        tab.getContent().setDisable(true);
                        tab.setGraphic(Images.SKULL.imageView());
                        Utils.showError(
                                String.format("Connection to '%s' was closed (by peer?)", alias.getName()),
                                "Please close the tab and reconnect.");
                    });
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("The connection was closed", e);
            }
        }
    }
}
