package io.jsrminer.uml.diff;

import io.rminerx.core.api.ISourceFile;

public class SourceFileMoveDiff extends SourceFileDiff {

    public SourceFileMoveDiff(ISourceFile originalClass, ISourceFile movedClass/*, UMLModelDiff modelDiff*/) {
        super(originalClass, movedClass/*, modelDiff*/);
    }

    public ISourceFile getMovedFile() {
        return super.getSource2();
    }
    public ISourceFile getOriginalFile() {
        return super.getSource1();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("file ");
        sb.append(getSource1().getDirectoryPath());
        sb.append(" was moved to ");
        sb.append(getSource2().getDirectoryPath());
        sb.append(System.lineSeparator());
        return sb.toString();
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof SourceFileMoveDiff) {
            SourceFileMoveDiff classMoveDiff = (SourceFileMoveDiff) o;
            return this.getSource1().equals(classMoveDiff.getSource1().getFilepath())
                    && this.getSource2().getFilepath().equals(classMoveDiff.getSource2().getFilepath());
        }
        return false;
    }
}