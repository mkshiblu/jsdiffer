package io.jsrminer.parser;

import io.jsrminer.sourcetree.SourceLocation;

public interface ErrorReporter {
    void reportError(SourceLocation location, String message);
    void reportWarning(SourceLocation location, String message);
}
