package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.getTextInSource;

public class ExpressionsVisitor {

    /**
     * Has Token operator and ParseTree operand;
     */
    public static final NodeProcessor<String, UnaryExpressionTree, ILeafFragment> unaryExpression
            = new NodeProcessor<>() {
        @Override
        public String process(UnaryExpressionTree tree, ILeafFragment leaf, IContainer container) {
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

    public static final INodeProcessor<String, BinaryOperatorTree, ILeafFragment> binaryOperatorProcessor
            = new INodeProcessor<>() {
        @Override
        public String process(BinaryOperatorTree tree, ILeafFragment leaf, IContainer container) {
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
    public static final NodeProcessor<Void, MemberLookupExpressionTree, ILeafFragment> memberLookupExpression
            = new NodeProcessor<>() {
        @Override
        public Void process(MemberLookupExpressionTree tree, ILeafFragment leaf, IContainer container) {

            Visitor.visitExpression(tree.operand, leaf, container);
            Visitor.visitExpression(tree.memberExpression, leaf, container);
            return null;
        }
    };

    /**
     * Has ParseTree operand & IdentifierToken memberName;
     */
    public static final NodeProcessor<Void, MemberExpressionTree, ILeafFragment> memberExpression
            = new NodeProcessor<>() {
        @Override
        public Void process(MemberExpressionTree tree, ILeafFragment leaf, IContainer container) {

            Visitor.visitExpression(tree.operand, leaf, container);
            leaf.getVariables().add(tree.memberName.value);
            return null;
        }
    };

    public static final NodeProcessor<Void, IdentifierExpressionTree, ILeafFragment> identifierProcessor
            = new NodeProcessor<>() {
        @Override
        public Void process(IdentifierExpressionTree tree, ILeafFragment leaf, IContainer container) {
            leaf.getVariables().add(tree.identifierToken.value);
            return null;
        }
    };

    /**
     * A comma expression d, x = "4";
     */
    public static final INodeProcessor<String, CommaExpressionTree, ILeafFragment> commaExpressionProcessor
            = new NodeProcessor<>() {
        @Override
        public String process(CommaExpressionTree tree, ILeafFragment leaf, IContainer container) {
            tree.expressions.forEach(expressionTree -> {
                Visitor.visitExpression(expressionTree, leaf, container);
            });
            return getTextInSource(tree);
        }
    };

}
