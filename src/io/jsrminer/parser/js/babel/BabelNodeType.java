package io.jsrminer.parser.js.babel;

import java.util.HashMap;
import java.util.Map;

public enum BabelNodeType {
    VARIABLE_DECLARATION("VariableDeclaration"),
    VARIABLE_DECLARATOR("VariableDeclarator"),
    NUMERIC_LITERAL("NumericLiteral"),
    STRING_LITERAL("StringLiteral"),
    BOOLEAN_LITERAL("BooleanLiteral"),
    FUNCTION_DECLARATION("FunctionDeclaration"),
    FUNCTION_EXPRESSION("FunctionExpression"),
    EXPRESSION_STATEMENT("ExpressionStatement"),
    ASSIGNMENT_EXPRESSION("AssignmentExpression"),
    MEMBER_EXPRESSION("MemberExpression"),
    UNARY_EXPRESSION("UnaryExpression"),
    UPDATE_EXPRESSION("UpdateExpression"),
    NEW_EXPRESSION("NewExpression"),
    THIS_EXPRESSION("ThisExpression"),
    IDENTIFIER("Identifier"),
    EMPTY_STATEMENT("EmptyStatement"),
    BLOCK_STATEMENT("BlockStatement"),
    OBJECT_EXPRESSION("ObjectExpression"),
    BINARY_EXPRESSION("BinaryExpression"),
    FILE("File"),
    PROGRAM("Program"),
    RETURN_STATEMENT("ReturnStatement"),
    BREAK_STATEMENT("BreakStatement"),
    CONTINUE_STATEMENT("ContinueStatement"),
    LABELLED_STATEMENT("LabelledStatement"),

    // Loops
    FOR_STATEMENT("ForStatement"),

    // Ex
    TRY_STATEMENT("TryStatement"),
    CATCH_CLAUSE("CatchClause"),
    THROW_STATEMENT("ThrowStatement"),

    IF_STATEMENT("IfStatement"),
    SWITCH_STATEMENT("SwitchStatement"),
    SWITCH_CASE("SwitchCase"),
    ARRAY_EXPRESSION("ArrayExpression"),
    LOGICAL_EXPRESSION("LogicalExpression"),
    CALL_EXPRESSION("CallExpression"),


    ;
    private static final Map<String, BabelNodeType> typeTitleCaseMap = new HashMap<>();

    static {
        for (var type : BabelNodeType.values()) {
            typeTitleCaseMap.put(type.titleCase, type);
        }
    }

    public final String titleCase;

    BabelNodeType(String titleCase) {
        this.titleCase = titleCase;
    }

    public static BabelNodeType fromTitleCase(String typeInTitleCase) {
        var babelNodeType = typeTitleCaseMap.get(typeInTitleCase);
        if (babelNodeType == null) {
            System.out.println("No Code Element Tpe for " + typeInTitleCase);
        }
        return babelNodeType;
    }
}
