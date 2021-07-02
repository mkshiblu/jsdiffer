package io.jsrminer.uml.diff;

import io.rminerx.core.api.ISourceFile;

public class SourceFileRenameDiff extends ContainerDiff {

    private final double normalizedNameDistance;
    private final double normalizedDirectoryPathDistance;

    ISourceFile originalFile;
    ISourceFile renamedFile;

    public SourceFileRenameDiff(ISourceFile originalFile, ISourceFile renamedFile/*, UMLModelDiff modelDiff*/) {
        super(originalFile, renamedFile/*, modelDiff*/);
        this.originalFile = originalFile;
        this.renamedFile = renamedFile;
        this.normalizedNameDistance = StringDistance.normalizedDistanceIgnoringCase(getRenamedFile().getName(), getOriginalFile().getName());
        this.normalizedDirectoryPathDistance = StringDistance.normalizedDistanceIgnoringCase(getRenamedFile().getDirectoryPath(), getOriginalFile().getDirectoryPath());
    }

    public ISourceFile getRenamedFile() {
        return renamedFile;
    }

    public ISourceFile getOriginalFile() {
        return originalFile;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("file ");
        sb.append(originalFile.getFilepath());
        sb.append(" was renamed to ");
        sb.append(renamedFile.getFilepath());
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
