package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.BinaryOperatorTree;
import com.google.javascript.jscomp.parsing.parser.trees.CommaExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.IdentifierExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.LiteralExpressionTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import java.util.List;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.getTextInSource;

public class ExpressionsVisitor {
    public static final NodeProcessor<String, IdentifierExpressionTree, CodeFragment> identifierProcessor
            = new NodeProcessor<>() {
        @Override
        public String process(IdentifierExpressionTree tree, CodeFragment fragment, IContainer container) {
            if (fragment instanceof BlockStatement) {
                throw new RuntimeException("Fragment should be a leaf" + fragment);
            }

            fragment.getVariables().add(tree.identifierToken.value);
            return tree.identifierToken.value;
        }
    };

    public static final NodeProcessor<String, LiteralExpressionTree, CodeFragment> literalExpressionProcessor
            = new NodeProcessor<>() {
        @Override
        public String process(LiteralExpressionTree tree, CodeFragment fragment, IContainer container) {
            if (fragment instanceof BlockStatement) {
                throw new RuntimeException("Fragment should be a leaf" + fragment);
            }

            List<String> literals;
            switch (tree.literalToken.type) {
                case NUMBER:
                    literals = fragment.getNumberLiterals();
                    break;
                case STRING:
                    literals = fragment.getStringLiterals();
                    break;
                case NULL:
                    literals = fragment.getNullLiterals();
                    break;
                default:
                    throw new RuntimeException("Literal type: " + tree.literalToken.type + " not handled");
            }

            literals.add(tree.literalToken.toString());
            return tree.literalToken.toString();
        }
    };

    public static final INodeProcessor<String, BinaryOperatorTree, ILeafFragment> binaryOperatorProcessor
            = new INodeProcessor<>() {
        @Override
        public String process(BinaryOperatorTree tree, ILeafFragment fragment, IContainer container) {
            if (fragment instanceof BlockStatement) {
                throw new RuntimeException("Fragment should be a leaf" + fragment);
            }

            var operator = tree.operator.toString();
            fragment.getInfixOperators().add(operator);
            Visitor.visitExpression(tree.left, fragment, container);
            Visitor.visitExpression(tree.right, fragment, container);
            //fragment.getVariables().add(tree.identifierToken.value);
            //return tree.identifierToken.value;
            return getTextInSource(tree);
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
