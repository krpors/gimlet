package cruft.wtf.gimlet.util;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible for exporting generic {@link javafx.scene.control.TableView} data
 * (stored as an ObservableList of ObservableLists) to copy/paste format. Whatever, we'll see.
 */
public class DataConverter {

    private static final String SEP = System.getProperty("line.separator");

    /**
     * Exports to a basic String representation using the given options.
     *
     * @param columnNames The column names.
     * @param tableData   The actual table data.
     * @param opts        The options.
     * @return The String.
     */
    public static String convertToText(
            final List<String> columnNames,
            final List<ObservableList> tableData,
            final Options opts) {

        boolean includeColNames = opts.isIncludeColNames();
        boolean fitWidth = opts.isFitWidth();
        String columnSeparator = opts.getColumnSeparator();
        columnSeparator = columnSeparator.replaceAll("\\\\t", "\t");

        int[] colWidths = new int[columnNames.size()];
        Arrays.fill(colWidths, 0);

        StringBuilder b = new StringBuilder();

        // We do two passes here. If fitWidth is set to true, we are determining the
        // maximum amount of characters which are used per column, so we can pad the
        // data in the output to get a nice looking table. If it's set to false however,
        // no padding will be done in the second pass.
        if (fitWidth) {
            // Iterate over the column names. They may be longer than the actual content.
            if (includeColNames) {
                for (int colIdx = 0; colIdx < columnNames.size(); colIdx++) {
                    colWidths[colIdx] = columnNames.get(colIdx).length();
                }
            }

            // Iterate over each row first.
            for (ObservableList row : tableData) {
                // Then check each column, determine the maximum size.
                for (int col = 0; col < row.size(); col++) {
                    String colData = String.valueOf(row.get(col));

                    // Don't append extra spaces at the last column
                    if (col == row.size() - 1) {
                        colWidths[col] = 0;
                    } else {
                        colWidths[col] = Math.max(colWidths[col], colData.length());
                    }
                }
            }
        }

        if (includeColNames) {
            for (int col = 0; col < columnNames.size(); col++) {
                b.append(padString(columnNames.get(col), colWidths[col], ' '));
                if (col < columnNames.size() - 1) {
                    b.append(columnSeparator);
                }
            }
            b.append(SEP);
        }

        // Iterate over all rows and columns, pad if necessary, and replace newlines,
        // carriage returns and tabs.
        for (ObservableList row : tableData) {
            // Then check each column, determine the maximum size.
            for (int col = 0; col < row.size(); col++) {
                String colData = String.valueOf(row.get(col));
                colData = colData.replace('\n', ' ');
                colData = colData.replace('\t', ' ');
                colData = colData.replace('\r', ' ');
                b.append(padString(colData, colWidths[col], ' '));
                if (col < row.size() - 1) {
                    b.append(columnSeparator);
                }
            }
            b.append(SEP);
        }


        return b.toString();
    }

    /**
     * Pads a string to the right with the requested padChar, to a maximum of len size, using the source
     * as a String.
     *
     * @param source  The String source.
     * @param len     The length to pad to.
     * @param padChar The padding character.
     * @return The new, padded String.
     */
    private static String padString(final String source, int len, char padChar) {
        StringBuilder b = new StringBuilder(source);
        int diff = len - source.length();
        if (diff > 0) {
            char[] fill = new char[diff];
            Arrays.fill(fill, padChar);
            b.append(fill);
        }

        return b.toString();
    }

    /**
     * Converts the columns and table data to a simplistic HTML table.
     *
     * @param columnNames The column names.
     * @param tableData   The table data.
     * @return The HTML string.
     */
    public static String convertToHtml(final List<String> columnNames, final ObservableList<ObservableList> tableData) {
        StringBuilder s = new StringBuilder();
        Escaper e = HtmlEscapers.htmlEscaper();

        s.append("<table>");
        s.append("<thead>");
        s.append("<tr>");
        for (String col : columnNames) {
            s.append("<th>").append(e.escape(String.valueOf(col))).append("</th>");
        }
        s.append("</tr>");
        s.append("</thead>");
        s.append("<tbody>");
        for (ObservableList cols : tableData) {
            s.append("<tr>");
            for (Object content : cols) {
                s.append("<td>").append(e.escape(String.valueOf(content))).append("</td>");
            }
            s.append("</tr>");
        }
        s.append("</tbody>");
        s.append("</table>");

        return s.toString();
    }

    public static class Options {
        private BooleanProperty includeColNames = new SimpleBooleanProperty(true);

        private BooleanProperty fitWidth = new SimpleBooleanProperty(false);

        private StringProperty columnSeparator = new SimpleStringProperty(" ");

        public boolean isIncludeColNames() {
            return includeColNames.get();
        }

        public BooleanProperty includeColNamesProperty() {
            return includeColNames;
        }

        public void setIncludeColNames(boolean includeColNames) {
            this.includeColNames.set(includeColNames);
        }

        public boolean isFitWidth() {
            return fitWidth.get();
        }

        public BooleanProperty fitWidthProperty() {
            return fitWidth;
        }

        public void setFitWidth(boolean fitWidth) {
            this.fitWidth.set(fitWidth);
        }

        public String getColumnSeparator() {
            return columnSeparator.get();
        }

        public StringProperty columnSeparatorProperty() {
            return columnSeparator;
        }

        public void setColumnSeparator(String columnSeparator) {
            this.columnSeparator.set(columnSeparator);
        }
    }
}
