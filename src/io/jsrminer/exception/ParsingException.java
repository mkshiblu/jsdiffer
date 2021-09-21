package io.jsrminer.exception;

import io.jsrminer.parser.SyntaxMessage;

public class ParsingException extends RuntimeException {

    private final SyntaxMessage syntaxMessage;

    public ParsingException(SyntaxMessage syntaxMessage) {
        super(syntaxMessage.getMessage());
        this.syntaxMessage = syntaxMessage;
    }

    @Override
    public String toString() {
        return syntaxMessage.getMessage();
    }
}
