package cruft.wtf.gimlet.ui.controls;

import javafx.scene.control.TextField;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Optional;

/**
 * A simple extension to the {@link TextField}, which contains some logic to parse a number.
 * When parsing fails, the user gets visual notification that it cannot be parsed correctly.
 */
public class NumberTextField extends TextField implements ParamInput {
    private DecimalFormat decimalFormat = new DecimalFormat("#");

    private Number number = 0;

    public NumberTextField() {
        this(0);
    }

    public NumberTextField(int initial) {
        setText(String.valueOf(initial));
        setNumber(getText());

        setOnKeyReleased(event -> setNumber(getText()));

        // When the text field loses focus, set the value of the text field to
        // the parsed number, just in case. The parse function does not have to
        // use all characters in the String, so things like '123.123salkdjask' are
        // still parsed correctly. This listener applies the actual parsed number
        // as the text field's contents.
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (number != null) {
                    setText(number.toString());
                }
            }
        });
    }

    private void setNumber(String text) {
        try {
            this.number = decimalFormat.parse(text);
            setStyle("");
        } catch (ParseException e) {
            // Number cannot be parsed. Mark it as such.
            number = null;
            setStyle("-fx-base: orangered");
        }
    }

    /**
     * Gets the number. If no number could be parsed, this {@link Optional} is empty.
     *
     * @return The optional number.
     */
    public Optional<Number> getNumber() {
        return Optional.ofNullable(number);
    }

    @Override
    public String getParameterName() {
        return getId();
    }

    @Override
    public Object getParameterValue() {
        return number;
    }

    @Override
    public void setParameterValue(Object o) {
        if (o instanceof Number) {
            Number n = (Number) o;
            this.number = n;
        } else {
            throw new ClassCastException("The given parameter is not a java.lang.Number and is a bug!");
        }
    }
}
