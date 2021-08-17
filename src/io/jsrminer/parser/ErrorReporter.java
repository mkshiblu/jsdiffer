package io.jsrminer.parser;

import com.google.javascript.jscomp.parsing.parser.util.SourcePosition;
import io.jsrminer.sourcetree.SourceLocation;

public interface ErrorReporter {
    void reportError(SourceLocation location, String message);
    void reportWarning(SourceLocation location, String message);
}
