package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.getTextInSource;

public class ExpressionsVisitor {

    /**
     * Represents UpdateExpression productions from the spec.
     *
     * <pre><code>
     * UpdateExpression :=
     *     { ++ | -- } UnaryExpression
     *     LeftHandSideExpression [no LineTerminator here] { ++ | -- }
     * </code></pre>
     */
    public static final NodeVisitor<String, UpdateExpressionTree, ILeafFragment> updateExpression
            = new NodeVisitor<>() {
        @Override
        public String visit(UpdateExpressionTree tree, ILeafFragment leaf, IContainer container) {
            String text = getTextInSource(tree);
            boolean isPrefixOperator = tree.operatorPosition == UpdateExpressionTree.OperatorPosition.PREFIX;

            if (isPrefixOperator) {
                leaf.getPrefixExpressions().add(text);
            } else {
                leaf.getPostfixExpressions().add(text);
            }

            Visitor.visitExpression(tree.operand, leaf, container);
            return text;
        }
    };

    /**
     * Has Token operator and ParseTree operand;
     */
    public static final NodeVisitor<String, UnaryExpressionTree, ILeafFragment> unaryExpression
            = new NodeVisitor<>() {
        @Override
        public String visit(UnaryExpressionTree tree, ILeafFragment leaf, IContainer container) {
            String text = getTextInSource(tree);
            String operator = tree.operator.type.toString();
            boolean isPrefixOperator = text.startsWith(operator);

            if (isPrefixOperator) {
                leaf.getPrefixExpressions().add(text);
            } else {
                leaf.getPostfixExpressions().add(text);
            }

            Visitor.visitExpression(tree.operand, leaf, container);
            return text;
        }
    };

    public static final INodeVisitor<String, BinaryOperatorTree, ILeafFragment> binaryOperatorProcessor
            = new INodeVisitor<>() {
        @Override
        public String visit(BinaryOperatorTree tree, ILeafFragment leaf, IContainer container) {
            String text = getTextInSource(tree);
            var operator = tree.operator.toString();

            leaf.getInfixOperators().add(operator);
            leaf.getInfixExpressions().add(text);

            Visitor.visitExpression(tree.left, leaf, container);
            Visitor.visitExpression(tree.right, leaf, container);
            return text;
        }
    };

    /**
     * E.g. config.keyCodes[key]
     * Has ParseTree operand (config.keyCodes) & ParseTree memberExpression (key)
     */
    public static final NodeVisitor<Void, MemberLookupExpressionTree, ILeafFragment> memberLookupExpression
            = new NodeVisitor<>() {
        @Override
        public Void visit(MemberLookupExpressionTree tree, ILeafFragment leaf, IContainer container) {

            Visitor.visitExpression(tree.operand, leaf, container);
            Visitor.visitExpression(tree.memberExpression, leaf, container);
            return null;
        }
    };

    /**
     * Has ParseTree operand & IdentifierToken memberName;
     */
    public static final NodeVisitor<Void, MemberExpressionTree, ILeafFragment> memberExpression
            = new NodeVisitor<>() {
        @Override
        public Void visit(MemberExpressionTree tree, ILeafFragment leaf, IContainer container) {

            Visitor.visitExpression(tree.operand, leaf, container);
            leaf.getVariables().add(tree.memberName.value);
            return null;
        }
    };

    public static final NodeVisitor<Void, IdentifierExpressionTree, ILeafFragment> identifierProcessor
            = new NodeVisitor<>() {
        @Override
        public Void visit(IdentifierExpressionTree tree, ILeafFragment leaf, IContainer container) {
            leaf.getVariables().add(tree.identifierToken.value);
            return null;
        }
    };

    /**
     * A comma expression d, x = "4";
     */
    public static final INodeVisitor<String, CommaExpressionTree, ILeafFragment> commaExpressionProcessor
            = new NodeVisitor<>() {
        @Override
        public String visit(CommaExpressionTree tree, ILeafFragment leaf, IContainer container) {
            tree.expressions.forEach(expressionTree -> {
                Visitor.visitExpression(expressionTree, leaf, container);
            });
            return getTextInSource(tree);
        }
    };
}
