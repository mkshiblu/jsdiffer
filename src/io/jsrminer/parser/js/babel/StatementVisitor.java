package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SingleStatement;
import io.rminerx.core.api.IContainer;

public class StatementVisitor {

    private final Visitor visitor;

    StatementVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * An expression statement, i.e., a statement consisting of a single expression.
     * interface ExpressionStatement<: Statement {
     * type: "ExpressionStatement";
     * expression: Expression;
     * }
     */
    public SingleStatement visitExpressionStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var leaf = visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, parent);
        visitor.visitExpression(node.get("expression"), leaf, container);
        return leaf;
    }


}
