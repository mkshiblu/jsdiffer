package io.jsrminer.sourcetree;

import java.util.regex.Pattern;

public class JsConfig {
    /**
     * Statement terminator in Js (semicolon)
     */
    public static final char STATEMENT_TERMINATOR_CHAR = ';';
    public static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile("[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])");
}
