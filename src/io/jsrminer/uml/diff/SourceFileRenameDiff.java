package io.jsrminer.uml.diff;

import io.rminerx.core.api.ISourceFile;

public class SourceFileRenameDiff extends SourceFileDiff {

    private final double normalizedNameDistance;
    private final double normalizedDirectoryPathDistance;

    public SourceFileRenameDiff(ISourceFile originalClass, ISourceFile renamedClass/*, UMLModelDiff modelDiff*/) {
        super(originalClass, renamedClass/*, modelDiff*/);
        this.normalizedNameDistance = StringDistance.normalizedDistanceIgnoringCase(getRenamedFile().getName(), getOriginalFile().getName());
        this.normalizedDirectoryPathDistance = StringDistance.normalizedDistanceIgnoringCase(getRenamedFile().getDirectoryPath(), getOriginalFile().getDirectoryPath());
    }

    public ISourceFile getRenamedFile() {
        return super.getSource2();
    }

    public ISourceFile getOriginalFile() {
        return super.getSource1();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("file ");
        sb.append(getSource1().getFilepath());
        sb.append(" was renamed to ");
        sb.append(getSource2().getFilepath());
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    public double getNormalizedNameDistance() {
        return this.normalizedNameDistance;
    }

    public double getNormalizedDiretoryPathDistance() {
        return this.normalizedDirectoryPathDistance;
    }
}
