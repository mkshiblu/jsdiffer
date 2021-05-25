package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.CodeElementType;

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
    NEW_EXPRESSION("NewExpression"),
    THIS_EXPRESSION("ThisExpression"),
    IDENTIFIER("Identifier"),
    EMPTY_STATEMENT("EmptyStatement"),
    BLOCK_STATEMENT("BlockStatement"),
    OBJECT_EXPRESSION("ObjectExpression"),
    FILE("File"),
    PROGRAM("Program"),

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
