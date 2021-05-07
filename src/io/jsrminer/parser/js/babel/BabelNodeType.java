package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.CodeElementType;

import java.util.HashMap;
import java.util.Map;

public enum BabelNodeType {
    VARIABLE_DECLARATION("VariableDeclaration");

    private String titleCase;

    BabelNodeType(String titleCase) {
        this.titleCase = titleCase;
    }

    private static Map<String, BabelNodeType> typeTitleCaseMap = new HashMap<>();

    static {
        for (BabelNodeType type : BabelNodeType.values()) {
            typeTitleCaseMap.put(type.titleCase, type);
        }
    }

    public static BabelNodeType fromTitleCase(String typeInTitleCase) {
        BabelNodeType t = typeTitleCaseMap.get(typeInTitleCase);
        if (t == null) {
            System.out.println("No Code Element Tpe for " + typeInTitleCase);
        }
        return t;
    }
}
