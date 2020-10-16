package io.jsrminer.sourcetree;

public abstract class Statement extends CodeFragment {

    protected Statement parent;

    public Statement() {

    }

    public Statement getParent() {
        return parent;
    }

    public void setParent(Statement parent) {
        this.parent = parent;
    }
}
