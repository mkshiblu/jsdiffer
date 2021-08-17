package io.jsrminer.parser;

/**
 * A user friendly error message class
 */
public class SyntaxMessage {
    private final String message;
    private final int line;
    private final int column;

    /**
     * Constructs a new syntax error message object.
     */
    public SyntaxMessage(String message, int line, int column) {
        this.message = message;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the source location.
     */
    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return line + "," + column + ": " + getMessage();
    }
}
