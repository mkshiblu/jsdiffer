package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.BinaryOperatorTree;
import com.google.javascript.jscomp.parsing.parser.trees.CommaExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.IdentifierExpressionTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.getTextInSource;

public class ExpressionsVisitor {
    public static final NodeProcessor<Void, IdentifierExpressionTree, ILeafFragment> identifierProcessor
            = new NodeProcessor<>() {
        @Override
        public Void process(IdentifierExpressionTree tree, ILeafFragment leaf, IContainer container) {
            leaf.getVariables().add(tree.identifierToken.value);
            return null;
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
