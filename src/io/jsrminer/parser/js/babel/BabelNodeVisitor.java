package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.jsrminer.parser.js.closurecompiler.INodeVisitor;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;

interface BabelNodeVisitor<R, N extends JV8, C extends ICodeFragment> {
    R visit(N node, C parent, IContainer container);
}
