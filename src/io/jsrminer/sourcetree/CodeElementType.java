package io.jsrminer.sourcetree;

import java.util.HashMap;
import java.util.Map;

public enum CodeElementType {
    EXPRESSION_STATEMENT("ExpressionStatement"),
    IF_STATEMENT("IfStatement"), BLOCK_STATEMENT("BlockStatement"),
    FUNCTION_DECLARATION("FunctionDeclaration"), RETURN_STATEMENT("ReturnStatement");

    public final String titleCase;

    private CodeElementType(String titleCase) {
        this.titleCase = titleCase;
    }

    private static Map<String, CodeElementType> typeTitleCaseMap = new HashMap<>();
    static {
        for (CodeElementType type : CodeElementType.values()) {
            typeTitleCaseMap.put(type.titleCase, type);
        }
    }

    public static CodeElementType getFromTitleCase(String typeInTitleCase) {
        return typeTitleCaseMap.get(typeInTitleCase);
    }
}
