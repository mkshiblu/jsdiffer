package io.jsrminer.sourcetree;

import java.util.regex.Pattern;

public class JsConfig {
    /**
     * Statement terminator in Js (semicolon)
     */
    public static final char STATEMENT_TERMINATOR_CHAR = ';';
    public static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile("[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])");
    public static final String JS_FILE_EXTENSION = "js";
    public static final String[] IGNORED_FILE_EXTENSIONS = {"min.js"};
    public static final int MAXIMUM_NUMBER_OF_COMPARED_METHODS = 100;
    public static final String TEXT_ASSIGNING_TRUE = " = true;";
    public static final String TEXT_ASSIGNING_FALSE = " = false;";

    // Mapper
    public static final String SPLIT_CONCAT_STRING_PATTERN = "(\\s)*(\\+)(\\s)*";

    // Parser specifics

    // If true, Functions of a leaf will be added to its parent containers list of anonymous function declarations too
    public static final boolean addLeafAnonymousFunctionsToParentContainerAlso = true;
    public static boolean treatUMDAsSourceFile = false;
}
