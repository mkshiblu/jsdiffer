package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ForStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Expression;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class LoopStatementsVisitor {

    /**
     * A Standard For loop e.g. for (int i =0; i<5; i++)
     * Has initializer, condition, increment and Body
     */

    public static final INodeVisitor<BlockStatement, ForStatementTree, BlockStatement> forStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(ForStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse initializer
            if (tree.initializer.type != ParseTreeType.NULL) {
                Expression initializer = createBaseExpressionWithRMType(tree.initializer, CodeElementType.FOR_STATEMENT_INITIALIZER);
                Visitor.visitExpression(tree.initializer, initializer, container);
                addExpression(initializer, composite);
            }
            // Parse condition
            if (tree.condition.type != ParseTreeType.NULL) {
                Expression conditionExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.FOR_STATEMENT);
                Visitor.visitExpression(tree.condition, conditionExpression, container);
                addExpression(conditionExpression, composite);
            }
            // Parse increment
            if (tree.increment.type != ParseTreeType.NULL) {
                Expression updateExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.FOR_STATEMENT);
                Visitor.visitExpression(tree.increment, updateExpression, container);
                addExpression(updateExpression, composite);
            }

            // Parse Body
            Visitor.visitStatement(tree.body, composite, container);
            return composite;
        }
    };
}
