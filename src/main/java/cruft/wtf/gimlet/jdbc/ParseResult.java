package cruft.wtf.gimlet.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class ParseResult {

    private String sql;

    private List<Param> parameters;

    public enum Type {
        NONE,
        STRING,
        NUMBER,
        DATE,
        DATETIME;
    }

    public static class Param implements Comparable<Param> {
        private final String name;

        private final Type dataType;

        public Param(String name, Type dataType) {
            this.name = name;
            this.dataType = dataType;
        }

        public String getName() {
            return name;
        }

        public Type getDataType() {
            return dataType;
        }

        @Override
        public String toString() {
            return "Param{" +
                    "name='" + name + '\'' +
                    ", dataType=" + dataType +
                    '}';
        }

        @Override
        public int compareTo(Param o) {
            if (o == null) {
                return -1;
            }

            return getName().compareTo(o.getName());
        }
    }

    /**
     * Parses the query.
     * @param query
     */
    public static ParseResult parse(final String query) {
        List<Param> parameters = new ArrayList<>();
        int length = query.length();
        StringBuilder parsedQuery = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;

        for (int i = 0; i < length; i++) {
            char c = query.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else if (inMultiLineComment) {
                if (c == '*' && query.charAt(i + 1) == '/') {
                    inMultiLineComment = false;
                }
            } else if (inSingleLineComment) {
                if (c == '\n') {
                    inSingleLineComment = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == '/' && query.charAt(i + 1) == '*') {
                    inMultiLineComment = true;
                } else if (c == '-' && query.charAt(i + 1) == '-') {
                    inSingleLineComment = true;
                } else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name = query.substring(i + 1, j);
                    StringBuilder type = new StringBuilder();

                    // This next part tries to read the input data type. This data type takes the form of
                    // something like: :id[data_type]. Everything between the square brackets should be
                    // interpreted as the data type.

                    // Amount of characters to skip after parsing the [...] part (bracket inclusive)
                    int skip = 0;
                    // The last index found of the [ character.
                    int lastIndexOfOpenBracket = -1;
                    // Whether a bracket has been closed.
                    boolean bracketClosed = false;
                    if (j < length && query.charAt(j) == '[') {
                        lastIndexOfOpenBracket = j;
                        j++;
                        skip++;
                        while (j < length) {
                            skip++;
                            if (query.charAt(j) == ']') {
                                bracketClosed = true;
                                break;
                            }
                            type.append(query.charAt(j));
                            j++;
                        }

                        if (lastIndexOfOpenBracket > 0 && !bracketClosed) {
                            System.out.println("No closing bracket found " + lastIndexOfOpenBracket);
                            throw new RuntimeException("Closing bracket ']' missing" + lastIndexOfOpenBracket);
                        }
                    }

                    Type dataType = Type.NONE;
                    try {
                        dataType = Type.valueOf(type.toString().trim().toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        // swallow, do nothing. Keep Type.NONE;
                    }

                    parameters.add(new Param(name, dataType));
                    c = '?'; // replaceChars the parameter with a question mark
                    i += name.length() + skip; // skip past the end of the parameter
                }
            }
            parsedQuery.append(c);
        }

        return new ParseResult(parsedQuery.toString(), parameters);
    }

    private ParseResult(String sql, List<Param> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getSql() {
        return sql;
    }

    List<Param> getParameters() {
        return parameters;
    }

    public Set<Param> getUniqueParameters() {
        return new TreeSet<>(parameters);
    }
}
