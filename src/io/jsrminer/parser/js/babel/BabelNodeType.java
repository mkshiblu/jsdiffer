package io.jsrminer.parser.js.babel;

import java.util.HashMap;
import java.util.Map;

public enum BabelNodeType {
    VARIABLE_DECLARATION("VariableDeclaration"),
    VARIABLE_DECLARATOR("VariableDeclarator"),
    NUMERIC_LITERAL("NumericLiteral"),
    STRING_LITERAL("StringLiteral"),
    NULL_LITERAL("NullLiteral"),
    TEMPLATE_LITERAL("TemplateLiteral"),
    REG_EXP_LITERAL("RegExpLiteral"),
    BOOLEAN_LITERAL("BooleanLiteral"),
    FUNCTION_DECLARATION("FunctionDeclaration"),
    CLASS_DECLARATION("ClassDeclaration"),
    CLASS_EXPRESSION("ClassExpression"),
    CLASS_BODY("ClassBody"),
    CLASS_METHOD("ClassMethod"),
    CLASS_PRIVATE_METHOD("ClassPrivateMethod"),
    CLASS_PROPERTY("ClassProperty"),
    CLASS_PRIVATE_PROPERTY("ClassPrivateProperty"),

    FUNCTION_EXPRESSION("FunctionExpression"),
    ARROW_FUNCTION_EXPRESSION("ArrowFunctionExpression"),
    EXPRESSION_STATEMENT("ExpressionStatement"),
    ASSIGNMENT_EXPRESSION("AssignmentExpression"),
    MEMBER_EXPRESSION("MemberExpression"),
    UNARY_EXPRESSION("UnaryExpression"),
    UPDATE_EXPRESSION("UpdateExpression"),
    NEW_EXPRESSION("NewExpression"),
    THIS_EXPRESSION("ThisExpression"),
    SUPER("Super"),
    SEQUENCE_EXPRESSION("SequenceExpression"),

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
    LABELED_STATEMENT("LabeledStatement"),

    // Loops
    FOR_STATEMENT("ForStatement"),
    WHILE_STATEMENT("WhileStatement"),
    DO_WHILE_STATEMENT("DoWhileStatement"),
    FOR_OF_STATEMENT("ForOfStatement"),
    FOR_IN_STATEMENT("ForInStatement"),

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
    CONDITIONAL_EXPRESSION("ConditionalExpression"),

    OBJECT_PROPERTY("ObjectProperty"),
    OBJECT_PATTERN("ObjectPattern"),
    ASSIGNMENT_PROPERTY("AssignmentProperty"),
    REST_ELEMENT("RestElement"),
    OBJECT_METHOD("ObjectMethod"),
    SPREAD_ELEMENT("SpreadElement"),
    ARRAY_PATTERN("ArrayPattern"),

    IMPORT_DECLARATION("ImportDeclaration"),
    IMPORT("Import"),
    EXPORT_DECLARATION("ExportDeclaration"),
    EXPORT_NAMED_DECLARATION("ExportNamedDeclaration"),
    EXPORT_DEFAULT_DECLARATION("ExportDefaultDeclaration"),
    OPT_CLASS_DECLARATION("OptClassDeclaration"),
    OPT_FUNCTION_DECLARATION("OptFunctionDeclaration"),
    TYPE_ALIAS("TypeAlias"),
    JSX_ELEMENT("JSXElement"),
    TYPE_CAST_EXPRESSION("TypeCastExpression"),
    AWAIT_EXPRESSION("AwaitExpression"),
    ASSIGNMENT_PATTERN("AssignmentPattern"),
    TAGGED_TEMPLATE_EXPRESSION("TaggedTemplateExpression"),
    YIELD_EXPRESSION("YieldExpression"),

    // Flow Types
    DECLARE_MODULE("DeclareModule"),
    DECLARE_FUNCTION("DeclareFunction"),
    DECLARE_CLASS("DeclareClass"),
    DECLARE_VARIABLE("DeclareVariable"),
    EXPORT_ALL_DECLARATION("ExportAllDeclaration"),
    DEBUGGER_STATEMENT("DebuggerStatement"),
    DECLARE_TYPE_ALIAS("DeclareTypeAlias"),
    INTERFACE_DECLARATION("InterfaceDeclaration"),

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
        return typeTitleCaseMap.get(typeInTitleCase);
    }
}
