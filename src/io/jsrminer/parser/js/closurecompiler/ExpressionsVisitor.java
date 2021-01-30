package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.IdentifierExpressionTree;
import io.jsrminer.sourcetree.CodeFragment;
import io.rminer.core.api.IContainer;

public class ExpressionsVisitor {
    public static final NodeProcessor<String, IdentifierExpressionTree, CodeFragment> identifierProcessor
            = new NodeProcessor<>() {
        @Override
        public String process(IdentifierExpressionTree tree, CodeFragment parent, IContainer container) {
            return tree.identifierToken.value;
        }
    };
}
