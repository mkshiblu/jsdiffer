package io.jsrminer.uml.diff;

import io.rminerx.core.api.ISourceFile;

/**
 * Represents a diff between two containers
 */
public class SourceFileDiff extends ContainerDiff {

    private final ISourceFile source1;
    private final ISourceFile source2;

    public SourceFileDiff(ISourceFile source1, ISourceFile source2) {
        super(source1, source2);
        this.source1 = source1;
        this.source2 = source2;
    }

    public ISourceFile getSource1() {
        return source1;
    }

    public ISourceFile getSource2() {
        return source2;
    }
}
