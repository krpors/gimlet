package cruft.wtf.gimlet.ui;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.eventbus.Subscribe;
import cruft.wtf.gimlet.event.EventDispatcher;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Table holding logging events.
 */
public class LogTable extends TableView<ILoggingEvent> {

    private static Logger logger = LoggerFactory.getLogger(LogTable.class);

    public LogTable() {
        EventDispatcher.getInstance().register(this);

        setStyle("-fx-font-size: 8pt");

        setEditable(false);
        setTableMenuButtonVisible(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        setRowFactory(param -> new Row());

        TableColumn<ILoggingEvent, Level> columnLevel = new TableColumn<>("Level");
        columnLevel.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getLevel()));
        columnLevel.setCellFactory(param -> new LevelCell());
        columnLevel.setMaxWidth(250);
        columnLevel.setSortable(false);
        getColumns().add(columnLevel);

        TableColumn<ILoggingEvent, Number> columnDateTime = new TableColumn<>("Timestamp");
        columnDateTime.setCellValueFactory(param -> new SimpleLongProperty(param.getValue().getTimeStamp()));
        columnDateTime.setCellFactory(param -> new TimestampCell());
        columnDateTime.setMaxWidth(650);
        columnDateTime.setSortable(false);
        getColumns().add(columnDateTime);

        TableColumn<ILoggingEvent, String> columnColName = new TableColumn<>("Message");
        columnColName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedMessage()));
        columnColName.setMinWidth(120);
        columnColName.setSortable(false);
        getColumns().add(columnColName);
    }

    /**
     * Listen for logging events.
     *
     * @param event The logging event by logback.
     */
    @Subscribe
    public void onLoggingEvent(final ILoggingEvent event) {
        getItems().add(event);
        Platform.runLater(() -> scrollToEnd());
    }

    public void scrollToEnd() {
        scrollTo(getItems().size());

        // TODO: configurable, and wha'ever. Pick better values.
        // arbitrary value for now. When amount of items exceed 30, remove the first 10.
        if (getItems().size() >= 250) {
            getItems().remove(0, 50);
        }
    }

    private static class Row extends TableRow<ILoggingEvent> {
        @Override
        protected void updateItem(ILoggingEvent item, boolean empty) {
            super.updateItem(item, empty);

            getStyleClass().remove("primary-key");

            if (item == null || empty) {
                return;
            }

            if (item.getLevel() == Level.ERROR) {
                getStyleClass().add("primary-key");
            }
        }
    }

    private static class LevelCell extends TextFieldTableCell<ILoggingEvent, Level> {
        @Override
        public void updateItem(Level item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                return;
            }

            switch (item.levelInt) {
                case Level.ERROR_INT:
                    setGraphic(Images.WARNING.imageView());
                    break;
                default:
                    setGraphic(null);
            }

            setText(item.levelStr);

        }
    }

    private static class TimestampCell extends TextFieldTableCell<ILoggingEvent, Number> {

        private static final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.S");

        @Override
        public void updateItem(Number item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                return;
            }

            setText(sdf.format(new Date(item.longValue())));
        }
    }
}
