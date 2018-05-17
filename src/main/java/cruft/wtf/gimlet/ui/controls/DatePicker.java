package cruft.wtf.gimlet.ui.controls;

import javafx.util.StringConverter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Specialization of JavaFX's {@link javafx.scene.control.DatePicker}. It implements the {@link ParamInput} interface
 * so it can be used in the {@link cruft.wtf.gimlet.ui.dialog.ParamInputDialog}.
 */
public class DatePicker extends javafx.scene.control.DatePicker implements ParamInput<Date> {

    private static final String defaultFormat = "yyyy-MM-dd";

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern(defaultFormat);

    public DatePicker() {
        setValue(LocalDate.now());
        setConverter(new InternalConverter());
    }

    @Override
    public String getParameterName() {
        return getId();
    }

    @Override
    public Date getParameterValue() {
        return Date.valueOf(getValue());
    }

    @Override
    public void setParameterValue(Date o) {
        setValue(o.toLocalDate());
    }

    class InternalConverter extends StringConverter<LocalDate> {
        public String toString(LocalDate object) {
            LocalDate value = getValue();
            if (value != null) {
                return value.format(dtf);
            }

            return "";
        }

        public LocalDate fromString(String value) {
            if (value == null || value.isEmpty()) {
                setValue(null);
                return null;
            }

            setValue(LocalDate.parse(value, dtf));
            return getValue();
        }
    }
}
