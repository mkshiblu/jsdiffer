package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.LiteralExpressionTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeFragment;
import io.rminerx.core.api.IContainer;

import java.util.List;

public class LiteralsProcessor {

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
}
