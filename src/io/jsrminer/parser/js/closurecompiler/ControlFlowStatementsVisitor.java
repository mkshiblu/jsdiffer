package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ReturnStatementTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SingleStatement;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.createSingleStatementPopulateAndAddToParent;

public class ControlFlowStatementsVisitor {
    /**
     * A return statement. Has expression
     */
    public static final NodeProcessor<SingleStatement, ReturnStatementTree, BlockStatement> returnStatementProcessor
            = new NodeProcessor<>() {
        @Override
        public SingleStatement process(ReturnStatementTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);
            if (tree.expression != null)
                Visitor.visitExpression(tree.expression, leaf, container);
            return leaf;
        }
    };
}
