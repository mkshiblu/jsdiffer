package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SingleStatement;
import io.rminerx.core.api.IContainer;

public class ControlFlowStatementVisitor {
    private final Visitor visitor;

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

    ;
//
//    /**
//     * A break statement which may have a name (label)
//     */
//    public static final NodeVisitor<SingleStatement, BreakStatementTree, BlockStatement> breakStatementProcessor
//            = new NodeVisitor<>() {
//        @Override
//        public SingleStatement visit(BreakStatementTree tree, BlockStatement parent, IContainer container) {
//            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);
//
//            //if (tree.name.value != null)
//            //  leaf.getVariables().add(tree.name.value);
//            return leaf;
//        }
//    };
//
//    /**
//     * A Continue statement which may have a level
//     */
//    public static final NodeVisitor<SingleStatement, ContinueStatementTree, BlockStatement> continueStatementProcessor
//            = new NodeVisitor<>() {
//        @Override
//        public SingleStatement visit(ContinueStatementTree tree, BlockStatement parent, IContainer container) {
//            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);
//
//            //if (tree.name.value != null)
//            //  leaf.getVariables().add(tree.name.value);
//            return leaf;
//        }
//    };
//
//    /**
//     * A labeled statement has name and statement
//     */
//    public static final NodeVisitor<Object, LabelledStatementTree, BlockStatement> labelledStatementProcessor
//            = new NodeVisitor<>() {
//        @Override
//        public Object visit(LabelledStatementTree tree, BlockStatement parent, IContainer container) {
//            return Visitor.visitStatement(tree.statement, parent, container);
//        }
//    };
}
