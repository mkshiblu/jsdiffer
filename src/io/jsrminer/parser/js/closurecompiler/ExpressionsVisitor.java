package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Expression;
import io.jsrminer.sourcetree.TernaryOperatorExpression;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class ExpressionsVisitor {

    /**
     * A conditional expression with condition, left and right
     * Can be consider as a ternary operator expression
     */
    public static final NodeVisitor<ILeafFragment, ConditionalExpressionTree, ILeafFragment> conditionalExpression
            = new NodeVisitor<>() {
        @Override
        public ILeafFragment visit(ConditionalExpressionTree tree, ILeafFragment leaf, IContainer container) {
            String text = getTextInSource(tree);
            Expression expression = createBaseExpressionWithRMType(tree.condition, CodeElementType.TERNARY_OPERATOR_CONDITION);
            Expression thenExpression = createBaseExpressionWithRMType(tree.left, CodeElementType.TERNARY_OPERATOR_THEN_EXPRESSION);
            Expression elseExpression = createBaseExpressionWithRMType(tree.right, CodeElementType.TERNARY_OPERATOR_ELSE_EXPRESSION);

            TernaryOperatorExpression ternaryOperatorExpression
                    = new TernaryOperatorExpression(text, expression, thenExpression, elseExpression);
            leaf.getTernaryOperatorExpressions().add(ternaryOperatorExpression);

            Visitor.visitExpression(tree.condition, expression, container);
            Visitor.visitExpression(tree.left, thenExpression, container);
            Visitor.visitExpression(tree.right, elseExpression, container);

            return leaf;
        }
    };

    public static final NodeVisitor<String, ThisExpressionTree, ILeafFragment> thisExpression
            = new NodeVisitor<>() {
        @Override
        public String visit(ThisExpressionTree tree, ILeafFragment leaf, IContainer container) {
            return getTextInSource(tree);
        }
    };

    /**
     * An expression with parenthesis such as case clause of switch
     */
    public static final NodeVisitor<Void, ParenExpressionTree, ILeafFragment> parenExpression
            = new NodeVisitor<>() {
        @Override
        public Void visit(ParenExpressionTree tree, ILeafFragment leaf, IContainer container) {
            Visitor.visitExpression(tree.expression, leaf, container);
            return null;
        }
    };

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

            // TODO should treated as infix if =?
            if (operator != "=") {
                leaf.getInfixOperators().add(operator);
                leaf.getInfixExpressions().add(text);
            }

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

            // Treat as array access
            leaf.getArrayAccesses().add(getTextInSource(tree));
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
            String variableName = tree.memberName.value;
            if (tree.operand.type == ParseTreeType.THIS_EXPRESSION) {
                variableName = "this." + variableName;
            }
            leaf.getVariables().add(variableName);
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

    public static final NodeVisitor<Void, NullTree, ILeafFragment> nullNodeProcessor
            = new NodeVisitor<>() {
        @Override
        public Void visit(NullTree tree, ILeafFragment leaf, IContainer container) {
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
