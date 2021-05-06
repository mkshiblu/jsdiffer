package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.CodeElementType;

import java.util.HashMap;
import java.util.Map;

public enum BabelNodeType {
    VARIABLE_DECLARATION("VariableDeclaration");

    private Object titleCase;

    BabelNodeType(String titleCase) {
        this.titleCase = titleCase;
    };

    private static Map<String, io.jsrminer.sourcetree.CodeElementType> typeTitleCaseMap = new HashMap<>();

    static {
        for (CodeElementType type : CodeElementType.values()) {
            typeTitleCaseMap.put(type.titleCase, type);
        }
    }

    public static CodeElementType fromTitleCase(String typeInTitleCase) {
        CodeElementType t = typeTitleCaseMap.get(typeInTitleCase);
        if (t == null) {
            System.out.println("No Code Element Tpe for " + typeInTitleCase);
        }
        return t;
    }
}
