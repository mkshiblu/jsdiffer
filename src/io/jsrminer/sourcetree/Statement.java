package io.jsrminer.sourcetree;

public abstract class Statement extends CodeFragment {

    protected int positionIndexInParent = -1;
    protected int depth = -1;

    public Statement() {

    }

    /**
     * Returns the nesting depth from the original declaring scope such as function body
     */
    public int getDepth() {
        return depth;
    }
}
