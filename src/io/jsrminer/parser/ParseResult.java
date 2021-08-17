package io.jsrminer.parser;
import java.util.List;

public class ParseResult<T> {

    private final List<SyntaxMessage> errors;

    private final List<SyntaxMessage> warnings;

    private T programAST;

    public ParseResult(T programAST, List<SyntaxMessage> errors, List<SyntaxMessage> warnings) {
        this.programAST = programAST;
        this.errors = errors;
        this.warnings = warnings;
    }

    /**
     * Returns the AST, or null if parse error.
     */
    public T getProgramAST() {
        return programAST;
    }

    /**
     * Returns the list of parse errors.
     */
    List<SyntaxMessage> getErrors() {
        return errors;
    }

    /**
     * Returns the list of parse warnings.
     */
    List<SyntaxMessage> getWarnings() {
        return warnings;
    }
}
