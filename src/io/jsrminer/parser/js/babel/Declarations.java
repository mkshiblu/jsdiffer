package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.parsing.parser.trees.ReturnStatementTree;
import io.jsrminer.parser.js.closurecompiler.NodeVisitor;
import io.jsrminer.parser.js.closurecompiler.Visitor;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.sourcetree.VariableDeclarationKind;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.entities.Container;

public class Declarations {
    /**
     * A return statement. Has expression
     */
    public static final BabelNodeVisitor<SingleStatement, JV8, BlockStatement> returnStatementProcessor
            = new BabelNodeVisitor<>() {
        @Override
        public SingleStatement visit(JV8 tree, BlockStatement parent, IContainer container) {
            return null;
        }
    };
}