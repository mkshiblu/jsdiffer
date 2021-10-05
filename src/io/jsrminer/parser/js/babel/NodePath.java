package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

class NodePath {
    private final IContainer container;
    private final BlockStatement parent;
    private final ILeafFragment leaf;

    NodePath(BlockStatement parent, IContainer container) {
        this.parent = parent;
        this.container = container;
        leaf = null;
    }

    NodePath(BlockStatement parent, IContainer container, ILeafFragment leaf) {
        this.parent = parent;
        this.container = container;
        this.leaf = leaf;
    }

    public BlockStatement getBlockParent() {
        return parent;
    }

    public ILeafFragment getLeaf() {
        return leaf;
    }

    public IContainer getContainer() {
        return container;
    }

    public String getNamespace() {
        return container.getQualifiedName();
    }
}
