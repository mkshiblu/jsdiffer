package io.jsrminer.sourcetree;

public abstract class Statement extends CodeFragment {

    protected BlockStatement parent;

    public Statement() {

    }

    public BlockStatement getParent() {
        return parent;
    }

    public void setParent(BlockStatement parent) {
        this.parent = parent;
    }

    public abstract int statementCount();
}
