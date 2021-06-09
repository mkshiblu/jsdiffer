package io.jsrminer.sourcetree;

import java.util.HashMap;
import java.util.Map;

public enum CodeElementType {
    EXPRESSION_STATEMENT("ExpressionStatement"),
    IF_STATEMENT("IfStatement", "if"),
    BLOCK_STATEMENT("BlockStatement", "{"),
    FUNCTION_DECLARATION("FunctionDeclaration"),
    EMPTY_STATEMENT("EmptyStatement"),
    FUNCTION_INVOCATION("CallExpression"),
    CONSTRUCTOR_INVOCATION("THIS_CONSTRUCTOR_INVOCATION"),    // TODO ReVisit type (this())
    SUPER_CONSTRUCTOR_INVOCATION("SuperExpression"),    // TODO ReVisit type (It could not be a constructor sometimes)
    OBJECT_CREATION("NewExpression"),
    RETURN_STATEMENT("ReturnStatement"),
    TRY_STATEMENT("TryStatement", "try"),
    CATCH_CLAUSE("CatchClause", "catch"),
    FINALLY_BLOCK("Finally", "finally"),
    THROW_STATEMENT("ThrowStatement"),
    ARRAY_EXPRESSION("ArrayExpression"),
    ENHANCED_FOR_STATEMENT("EnhancedForStatement", "for"), // TODO revisit
    FOR_STATEMENT("ForStatement", "for"),
    LABELED_STATEMENT("LabeledStatement"),
    VARIABLE_DECLARATION("VariableDeclaration"),
    DO_WHILE_STATEMENT("DoWhileStatement", "do"),
    BREAK_STATEMENT("BreakStatement"),
    CONTINUE_STATEMENT("ContinueStatement"),
    SWITCH_CASE("SwitchCase"),
    SWITCH_STATEMENT("SwitchStatement", "switch"),

    WHILE_STATEMENT("WhileStatement", "while"),
    LITERAL_EXPRESSION("LiteralExpression"),
    COMMA_EXPRESSION("CommaExpression"),
    VARIABLE_DECLARATION_STATEMENT("VariableDeclarationStatement"),
    IF_STATEMENT_CONDITION,
    VARIABLE_DECLARATION_INITIALIZER,
    FOR_STATEMENT_INITIALIZER,
    FOR_STATEMENT_CONDITION,
    FOR_STATEMENT_UPDATER,
    ENHANCED_FOR_STATEMENT_INITIALIZER, // SAME as RM EFS parameter
    ENHANCED_FOR_STATEMENT_EXPRESSION,
    WHILE_STATEMENT_CONDITION,
    DO_STATEMENT_CONDITION,
    CATCH_CLAUSE_EXCEPTION_NAME,
    SWITCH_STATEMENT_CONDITION,
    TERNARY_OPERATOR_CONDITION,
    TERNARY_OPERATOR_THEN_EXPRESSION,
    TERNARY_OPERATOR_ELSE_EXPRESSION,
    LIST_OF_STATEMENTS,
    FIELD_DECLARATION,
    ;

    public final String titleCase;
    public final String keyword;

    CodeElementType() {
        this.titleCase = null;
        this.keyword = null;
    }

    CodeElementType(String titleCase) {
        this.titleCase = titleCase;
        this.keyword = null;
    }

    CodeElementType(String titleCase, String keyword) {
        this.titleCase = titleCase;
        this.keyword = keyword;
    }

    private static Map<String, CodeElementType> typeTitleCaseMap = new HashMap<>();

    static {
        for (CodeElementType type : CodeElementType.values()) {
            typeTitleCaseMap.put(type.titleCase, type);
        }
    }

    public static CodeElementType getFromTitleCase(String typeInTitleCase) {
        CodeElementType t = typeTitleCaseMap.get(typeInTitleCase);
        if (t == null) {
            System.out.println("No Code Element Tpe for " + typeInTitleCase);
        }
        return t;
    }
}
