package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;

public interface BabelNodeVisitor<C extends ICodeFragment, R> {
    R visit(BabelNode node, C parent, IContainer container);
}