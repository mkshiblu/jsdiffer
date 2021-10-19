package io.jsrminer.uml.diff;

import io.rminerx.core.api.ISourceFile;

public class SourceFileMoveDiff extends ContainerDiff implements Comparable<SourceFileMoveDiff> {

    private ISourceFile originalFile;
    private ISourceFile movedFile;

    public SourceFileMoveDiff(ISourceFile originalFile, ISourceFile movedFile/*, UMLModelDiff modelDiff*/) {
        super(originalFile, movedFile/*, modelDiff*/);
        this.originalFile = originalFile;
        this.movedFile = movedFile;
    }

    public ISourceFile getMovedFile() {
        return movedFile;
    }

    public ISourceFile getOriginalFile() {
        return originalFile;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("file ");
        sb.append(getOriginalFile().getDirectoryPath());
        sb.append(" was moved to ");
        sb.append(getMovedFile().getDirectoryPath());
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof SourceFileMoveDiff) {
            SourceFileMoveDiff classMoveDiff = (SourceFileMoveDiff) o;
            return this.getOriginalFile().getFilepath().equals(classMoveDiff.getOriginalFile().getFilepath())
                    && this.getMovedFile().getFilepath().equals(classMoveDiff.getMovedFile().getFilepath());
        }
        return false;
    }

    @Override
    public int compareTo(SourceFileMoveDiff o) {
        return this.getOriginalFile().getFilepath().compareTo(o.getOriginalFile().getFilepath());
    }
}
