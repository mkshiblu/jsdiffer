package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;

class NodePath {
    private final IContainer container;
    private final ICodeFragment parent;

    NodePath(IContainer container, ICodeFragment parent) {
        this.container = container;
        this.parent = parent;
    }

    public ICodeFragment getParent() {
        return parent;
    }

    public IContainer getContainer() {
        return container;
    }

    public String getNamespace() {
        return container.getQualifiedName();
    }
}
