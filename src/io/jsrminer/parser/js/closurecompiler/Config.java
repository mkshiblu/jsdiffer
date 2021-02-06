package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;

import java.util.HashSet;
import java.util.Set;

public class Config {
    public static final Set<ParseTreeType> ignoredNodes = new HashSet<>() {{
        add(ParseTreeType.IMPORT_DECLARATION);
        add(ParseTreeType.EXPORT_DECLARATION);
    }};
}
