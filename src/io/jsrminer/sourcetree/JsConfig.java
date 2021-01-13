package io.jsrminer.sourcetree;

import java.util.regex.Pattern;

public class JsConfig {
    /**
     * Statement terminator in Js (semicolon)
     */
    public static final char STATEMENT_TERMINATOR_CHAR = ';';
    public static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile("[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])");
    public static final String JS_FILE_EXTENSION = ".js";
    public static final int MAXIMUM_NUMBER_OF_COMPARED_METHODS = 100;
}
