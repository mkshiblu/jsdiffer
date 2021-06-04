package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import io.jsrminer.sourcetree.CodeElementType;

import java.util.*;

public class BabelParserConfig {
    public static final Set<BabelNodeType> ignoredNodeTypes = Set.of(
            BabelNodeType.EMPTY_STATEMENT,
            //BabelNodeType.EXPORT_DECLARATION,
            BabelNodeType.IMPORT_DECLARATION,
            BabelNodeType.TYPE_ALIAS
    );

    public final static EnumMap<BabelNodeType, CodeElementType> babelNodeToCodeElementTypeMap =
            new EnumMapBuilder<BabelNodeType, CodeElementType>(BabelNodeType.class)
                    .put(BabelNodeType.BLOCK_STATEMENT, CodeElementType.BLOCK_STATEMENT)
                    .put(BabelNodeType.EXPRESSION_STATEMENT, CodeElementType.EXPRESSION_STATEMENT)
                    .put(BabelNodeType.NEW_EXPRESSION, CodeElementType.CONSTRUCTOR_INVOCATION)
                    .put(BabelNodeType.RETURN_STATEMENT, CodeElementType.RETURN_STATEMENT)
                    .put(BabelNodeType.VARIABLE_DECLARATION, CodeElementType.VARIABLE_DECLARATION)
                    //        put(ParseTreeType.NEW_EXPRESSION, CodeElementType.OBJECT_CREATION);
                    .put(BabelNodeType.IF_STATEMENT, CodeElementType.IF_STATEMENT)
                    .put(BabelNodeType.SWITCH_STATEMENT, CodeElementType.SWITCH_STATEMENT)
                    .put(BabelNodeType.SWITCH_CASE, CodeElementType.SWITCH_CASE)
//        put(ParseTreeType.DEFAULT_CLAUSE, CodeElementType.SWITCH_CASE);
//
//        put(ParseTreeType.FUNCTION_DECLARATION, CodeElementType.FUNCTION_DECLARATION);
//        put(ParseTreeType.EMPTY_STATEMENT, CodeElementType.EMPTY_STATEMENT);
                    .put(BabelNodeType.CALL_EXPRESSION, CodeElementType.FUNCTION_INVOCATION)
//        //put(ParseTreeType., CodeElementType.SUPER_CONSTRUCTOR_INVOCATION);
                    .put(BabelNodeType.TRY_STATEMENT, CodeElementType.TRY_STATEMENT)
                    .put(BabelNodeType.CATCH_CLAUSE, CodeElementType.CATCH_CLAUSE)
//        put(ParseTreeType.FINALLY, CodeElementType.FINALLY_BLOCK);
                    .put(BabelNodeType.THROW_STATEMENT, CodeElementType.THROW_STATEMENT)
//
//        //put(ParseTreeType.ARRAY_LITERAL_EXPRESSION, CodeElementType.ARRAY_EXPRESSION);
                    .put(BabelNodeType.FOR_STATEMENT, CodeElementType.FOR_STATEMENT)
                    .put(BabelNodeType.FOR_IN_STATEMENT, CodeElementType.ENHANCED_FOR_STATEMENT)
                    .put(BabelNodeType.FOR_OF_STATEMENT, CodeElementType.ENHANCED_FOR_STATEMENT)
                    .put(BabelNodeType.DO_WHILE_STATEMENT, CodeElementType.DO_WHILE_STATEMENT)
                    .put(BabelNodeType.WHILE_STATEMENT, CodeElementType.WHILE_STATEMENT)
//
                    .put(BabelNodeType.CONTINUE_STATEMENT, CodeElementType.CONTINUE_STATEMENT)
                    .put(BabelNodeType.BREAK_STATEMENT, CodeElementType.BREAK_STATEMENT)
                    .put(BabelNodeType.LABELLED_STATEMENT, CodeElementType.LABELED_STATEMENT)

                    .put(BabelNodeType.EXPORT_DEFAULT_DECLARATION, CodeElementType.EXPRESSION_STATEMENT)
//
//        put(ParseTreeType.VARIABLE_STATEMENT, CodeElementType.VARIABLE_DECLARATION_STATEMENT);
//        put(ParseTreeType.VARIABLE_DECLARATION, CodeElementType.VARIABLE_DECLARATION);
//        put(ParseTreeType.LITERAL_EXPRESSION, CodeElementType.LITERAL_EXPRESSION);
                    .build();
    /**
     * require('./core/core.js')(p1, p2)
     */
    public static boolean treatCallExpressionOperandAsTheFunctionName = true;

    public static boolean appendSemicolonToStatementIfNotPresent = true;
}
