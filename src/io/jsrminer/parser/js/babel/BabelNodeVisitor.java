package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;

public interface BabelNodeVisitor<R, C extends ICodeFragment> {
    R visit(BabelNode node, C parent, IContainer container);
}