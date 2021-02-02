package io.jsrminer.uml.diff;

import io.rminerx.core.api.ISourceFile;

public class SourceFileMoveDiff extends SourceFileDiff {

    public SourceFileMoveDiff(ISourceFile originalClass, ISourceFile movedClass/*, UMLModelDiff modelDiff*/) {
        super(originalClass, movedClass/*, modelDiff*/);
    }

    public ISourceFile getMovedFile() {
        return source2;
    }
    public ISourceFile getOriginalFile() {
        return source1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("file ");
        sb.append(source1.getDirectoryPath());
        sb.append(" was moved to ");
        sb.append(source2.getDirectoryPath());
        sb.append(System.lineSeparator());
        return sb.toString();
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof SourceFileMoveDiff) {
            SourceFileMoveDiff classMoveDiff = (SourceFileMoveDiff) o;
            return this.source1.equals(classMoveDiff.source1.getFilepath())
                    && this.source2.getFilepath().equals(classMoveDiff.source2.getFilepath());
        }
        return false;
    }
}