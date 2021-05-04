package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.jsrminer.parser.js.closurecompiler.INodeVisitor;
import io.jsrminer.sourcetree.CodeEntity;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.entities.Container;

public interface BabelNodeVisitor<R, F extends ICodeFragment> {
    R visit(JV8 node, F parentFragment, IContainer container);
}
