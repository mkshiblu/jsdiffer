package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeEntity;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import org.apache.commons.lang3.NotImplementedException;

import java.util.EnumMap;

import static com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType.*;

public class Visitor {
    private final static EnumMap<ParseTreeType, INodeVisitor<CodeEntity, ParseTree, ICodeFragment>> nodeProcessors
            = new EnumMap(ParseTreeType.class) {{

        //Declarations
        put(FUNCTION_DECLARATION, DeclarationsVisitor.functionDeclarationProcessor);
        put(VARIABLE_DECLARATION_LIST, DeclarationsVisitor.variableDeclarationsList);
        put(OBJECT_LITERAL_EXPRESSION, ObjectsVisitor.objectLiteralExpression);


        put(EXPRESSION_STATEMENT, StatementsVisitor.expressionStatementProcessor);
        put(BLOCK, StatementsVisitor.blockStatementProcessor);
        put(IF_STATEMENT, ChoiceStatementsVisitor.ifStatementProcessor);
        put(VARIABLE_STATEMENT, StatementsVisitor.variableStatementProcessor);

        put(COMMA_EXPRESSION, ExpressionsVisitor.commaExpressionProcessor);
        put(IDENTIFIER_EXPRESSION, ExpressionsVisitor.identifierProcessor);
        put(LITERAL_EXPRESSION, LiteralsProcessor.literalExpressionProcessor);

        put(NEW_EXPRESSION, InvocationsProcessor.newExpression);
        put(CALL_EXPRESSION, InvocationsProcessor.callExpression);

        put(BINARY_OPERATOR, ExpressionsVisitor.binaryOperatorProcessor);
        put(UNARY_EXPRESSION, ExpressionsVisitor.unaryExpression);
        put(UPDATE_EXPRESSION, ExpressionsVisitor.updateExpression);
        put(MEMBER_LOOKUP_EXPRESSION, ExpressionsVisitor.memberLookupExpression);
        put(MEMBER_EXPRESSION, ExpressionsVisitor.memberExpression);

        // Control Flow
        put(RETURN_STATEMENT, ControlFlowStatementsVisitor.returnStatementProcessor);


        // Loops
        put(FOR_STATEMENT, LoopStatementsVisitor.forStatementProcessor);
    }};

    static void visitExpression(ParseTree tree, ILeafFragment leaf, IContainer container) {
        var processor = nodeProcessors.get(tree.type);

        if (processor == null) {
            throw new NotImplementedException("Processor not implemented for " + tree.type);
        }

        // enter(tree, leaf, container);
        processor.visit(tree, leaf, container);
        //exit(tree, leaf, container);
    }

    static void visitStatement(ParseTree tree, BlockStatement parent, IContainer container) {
        var processor = nodeProcessors.get(tree.type);

        if (processor == null) {
            throw new NotImplementedException("Processor not implemented for " + tree.type);
        }

        enterStatement(tree, parent, container);
        processor.visit(tree, parent, container);
        exitStatement(tree, parent, container);
    }

    static void enterStatement(ParseTree tree, BlockStatement parent, IContainer container) {

    }

    static void exitStatement(ParseTree tree, BlockStatement parent, IContainer container) {

    }
}
