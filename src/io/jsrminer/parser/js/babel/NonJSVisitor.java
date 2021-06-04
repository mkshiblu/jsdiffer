package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

public class NonJSVisitor {
    BabelNodeVisitor<ILeafFragment, Object> typeAliasVisitor = (BabelNode node, ILeafFragment fragment, IContainer container) -> {
        return visitTypeAlias(node, fragment, container);
    };

    /**
     * id, right, type parameters
     *
     * @return
     */
    String visitTypeAlias(BabelNode node, ILeafFragment leaf, IContainer container) {

        return null;
    }
}
