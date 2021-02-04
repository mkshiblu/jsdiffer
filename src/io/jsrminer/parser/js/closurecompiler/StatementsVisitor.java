package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.BlockTree;
import com.google.javascript.jscomp.parsing.parser.trees.ExpressionStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableStatementTree;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class StatementsVisitor {

    /**
     * A Block Statement
     */
    public static final INodeVisitor<BlockStatement, BlockTree, BlockStatement> blockStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(BlockTree tree, BlockStatement parent, IContainer container) {
            var blockStatement = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse statements
            tree.statements.forEach(statementTree -> {
                Visitor.visitStatement(statementTree, blockStatement, container);
            });

            return blockStatement;
        }
    };

    /**
     * An expression statement such as x = "4";
     */
    public static final NodeVisitor<SingleStatement, ExpressionStatementTree, BlockStatement> expressionStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public SingleStatement visit(ExpressionStatementTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);
            Visitor.visitExpression(tree.expression, leaf, container);
            return leaf;
        }
    };

    /**
     * A Variable declaration Statement e.g. let x = "4";
     */
    public static final NodeVisitor<SingleStatement, VariableStatementTree, BlockStatement> variableStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public SingleStatement visit(VariableStatementTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);
            Visitor.visitExpression(tree.declarations, leaf, container);
            return leaf;
        }
    };
}
