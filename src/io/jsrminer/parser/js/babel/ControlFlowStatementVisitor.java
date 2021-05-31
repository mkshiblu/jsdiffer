package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SingleStatement;
import io.rminerx.core.api.IContainer;

public class ControlFlowStatementVisitor {
    private final Visitor visitor;

    BabelNodeVisitor<BlockStatement, SingleStatement> returnStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitReturnStatement(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, SingleStatement> breakStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitBreakStatement(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, SingleStatement> continueStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitContinueStatement(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, Object> lablelledStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitLabelledStatement(node, parent, container);
    };

    public ControlFlowStatementVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * // interface ReturnStatement <: Statement {
     * //     type: "ReturnStatement";
     * //     argument: Expression | null;
     * //   }
     */
    public SingleStatement visitReturnStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var leaf = visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, parent);

        var argumentNode = node.get("argument");
        if (argumentNode != null && argumentNode.isDefined())
            visitor.visitExpression(node.get("argument"), leaf, container);
        return leaf;
    }

    /**
     * interface BreakStatement <: Statement {
     * type: "BreakStatement";
     * label: Identifier | null;
     * }
     */
    public SingleStatement visitBreakStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var leaf = visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, parent);

        //if (tree.name.value != null)
        //  leaf.getVariables().add(tree.name.value);
        return leaf;
    }

    /**
     * interface ContinueStatement <: Statement {
     * type: "ContinueStatement";
     * label: Identifier | null;
     * }
     */
    SingleStatement visitContinueStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var leaf = visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, parent);

        //if (tree.name.value != null)
        //  leaf.getVariables().add(tree.name.value);
        return leaf;
    }

    /**
     * interface LabeledStatement <: Statement {
     * type: "LabeledStatement";
     * label: Identifier;
     * body: Statement;
     * }
     */
    Object visitLabelledStatement(BabelNode node, BlockStatement parent, IContainer container) {
        return visitor.visitStatement(node.get("body"), parent, container);
    }
}
