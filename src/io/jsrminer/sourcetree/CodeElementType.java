package io.jsrminer.sourcetree;

import java.util.HashMap;
import java.util.Map;

public enum CodeElementType {
    EXPRESSION_STATEMENT("ExpressionStatement"),
    IF_STATEMENT("IfStatement"),
    BLOCK_STATEMENT("BlockStatement"),
    FUNCTION_DECLARATION("FunctionDeclaration"),
    EMPTY_STATEMENT("EmptyStatement"),
    FUNCTION_INVOCATION("CallExpression"),
    CONSTRUCTOR_INVOCATION("THIS_CONSTRUCTOR_INVOCATION"),    // TODO ReVisit type (this())
    SUPER_CONSTRUCTOR_INVOCATION("SuperExpression"),    // TODO ReVisit type (It could not be a constructor sometimes)
    OBJECT_CREATION("NewExpression"),
    RETURN_STATEMENT("ReturnStatement"),
    TRY_STATEMENT("TryStatement"),
    CATCH_CLAUSE("CatchClause"),
    ARRAY_EXPRESSION("ArrayExpression"),
    ENHANCED_FOR_STATEMENT("ENHANCED_FOR_STATEMENT"), // TODO revisit
    FOR_STATEMENT("ForStatement");

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
