package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import io.jsrminer.sourcetree.CodeElementType;

import java.util.EnumMap;
import java.util.EnumSet;

import static com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType.*;

public class ClosureParserConfig {
    static final EnumSet<ParseTreeType> ignoredNodes = EnumSet.of(
            IMPORT_DECLARATION
            , MISSING_PRIMARY_EXPRESSION
            , EMPTY_STATEMENT
            , CLASS_DECLARATION
            , TEMPLATE_LITERAL_EXPRESSION
            , ITER_SPREAD
            , WITH_STATEMENT
    );

    final static EnumMap<ParseTreeType, CodeElementType> parseTreeTypeCodeElementTypeMap = new EnumMap(ParseTreeType.class) {{
        put(ParseTreeType.EXPRESSION_STATEMENT, CodeElementType.EXPRESSION_STATEMENT);
        put(EXPORT_DECLARATION, CodeElementType.EXPRESSION_STATEMENT);

        put(ParseTreeType.IF_STATEMENT, CodeElementType.IF_STATEMENT);
        put(ParseTreeType.SWITCH_STATEMENT, CodeElementType.SWITCH_STATEMENT);
        put(ParseTreeType.CASE_CLAUSE, CodeElementType.SWITCH_CASE);
        put(ParseTreeType.DEFAULT_CLAUSE, CodeElementType.SWITCH_CASE);

        put(ParseTreeType.BLOCK, CodeElementType.BLOCK_STATEMENT);
        put(ParseTreeType.FUNCTION_DECLARATION, CodeElementType.FUNCTION_DECLARATION);
        put(ParseTreeType.EMPTY_STATEMENT, CodeElementType.EMPTY_STATEMENT);
        put(ParseTreeType.CALL_EXPRESSION, CodeElementType.FUNCTION_INVOCATION);
        put(ParseTreeType.NEW_EXPRESSION, CodeElementType.CONSTRUCTOR_INVOCATION);
        //put(ParseTreeType., CodeElementType.SUPER_CONSTRUCTOR_INVOCATION);

//        put(ParseTreeType.NEW_EXPRESSION, CodeElementType.OBJECT_CREATION);

        put(ParseTreeType.TRY_STATEMENT, CodeElementType.TRY_STATEMENT);
        put(ParseTreeType.CATCH, CodeElementType.CATCH_CLAUSE);
        put(ParseTreeType.FINALLY, CodeElementType.FINALLY_BLOCK);
        put(ParseTreeType.THROW_STATEMENT, CodeElementType.THROW_STATEMENT);

        //put(ParseTreeType.ARRAY_LITERAL_EXPRESSION, CodeElementType.ARRAY_EXPRESSION);
        put(ParseTreeType.FOR_STATEMENT, CodeElementType.FOR_STATEMENT);
        put(ParseTreeType.FOR_IN_STATEMENT, CodeElementType.ENHANCED_FOR_STATEMENT);
        put(ParseTreeType.FOR_OF_STATEMENT, CodeElementType.ENHANCED_FOR_STATEMENT);
        put(ParseTreeType.DO_WHILE_STATEMENT, CodeElementType.DO_WHILE_STATEMENT);
        put(ParseTreeType.WHILE_STATEMENT, CodeElementType.WHILE_STATEMENT);

        put(ParseTreeType.CONTINUE_STATEMENT, CodeElementType.CONTINUE_STATEMENT);
        put(ParseTreeType.BREAK_STATEMENT, CodeElementType.BREAK_STATEMENT);

        put(ParseTreeType.LABELLED_STATEMENT, CodeElementType.LABELED_STATEMENT);
        put(ParseTreeType.RETURN_STATEMENT, CodeElementType.RETURN_STATEMENT);

        put(ParseTreeType.VARIABLE_STATEMENT, CodeElementType.VARIABLE_DECLARATION_STATEMENT);
        put(ParseTreeType.VARIABLE_DECLARATION, CodeElementType.VARIABLE_DECLARATION);

        put(ParseTreeType.LITERAL_EXPRESSION, CodeElementType.LITERAL_EXPRESSION);
    }};

    /**
     * require('./core/core.js')(p1, p2)
     */
    static boolean treatCallExpressionOperandAsTheFunctionName = true;

    static boolean appendSemicolonToStatementIfNotPresent = true;
}
