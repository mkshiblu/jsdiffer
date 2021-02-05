package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.LiteralExpressionTree;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import java.util.List;

public class LiteralsProcessor {

    public static final NodeVisitor<String, LiteralExpressionTree, ILeafFragment> literalExpressionProcessor
            = new NodeVisitor<>() {
        @Override
        public String visit(LiteralExpressionTree tree, ILeafFragment fragment, IContainer container) {

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
                case TRUE:
                case FALSE:
                    literals = fragment.getBooleanLiterals();
                    break;
                default:
                    throw new RuntimeException("Literal type: " + tree.literalToken.type + " not handled");
            }

            literals.add(tree.literalToken.toString());
            return tree.literalToken.toString();
        }
    };
}
