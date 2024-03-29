package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.BreakStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.ContinueStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.LabelledStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.ReturnStatementTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SingleStatement;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.createSingleStatementPopulateAndAddToParent;

public class ControlFlowStatementsVisitor {
    /**
     * A return statement. Has expression
     */
    public static final NodeVisitor<SingleStatement, ReturnStatementTree, BlockStatement> returnStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public SingleStatement visit(ReturnStatementTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);
            if (tree.expression != null)
                Visitor.visitExpression(tree.expression, leaf, container);
            return leaf;
        }
    };

    /**
     * A break statement which may have a name (label)
     */
    public static final NodeVisitor<SingleStatement, BreakStatementTree, BlockStatement> breakStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public SingleStatement visit(BreakStatementTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);

            //if (tree.name.value != null)
            //  leaf.getVariables().add(tree.name.value);
            return leaf;
        }
    };

    /**
     * A Continue statement which may have a level
     */
    public static final NodeVisitor<SingleStatement, ContinueStatementTree, BlockStatement> continueStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public SingleStatement visit(ContinueStatementTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);

            //if (tree.name.value != null)
            //  leaf.getVariables().add(tree.name.value);
            return leaf;
        }
    };

    /**
     * A labeled statement has name and statement
     */
    public static final NodeVisitor<Object, LabelledStatementTree, BlockStatement> labelledStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public Object visit(LabelledStatementTree tree, BlockStatement parent, IContainer container) {
            return Visitor.visitStatement(tree.statement, parent, container);
        }
    };
}
