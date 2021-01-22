package io.rminer.core.entities;

import io.jsrminer.uml.diff.StringDistance;
import io.rminer.core.api.ISourceFile;
import org.apache.commons.io.FilenameUtils;

/**
 * Represents a Source File model
 */
public class SourceFile extends Container implements ISourceFile {
    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    protected String qualifiedName;
    private String filepath;
    private String name;
    private String directoryPath;

    public SourceFile() {
        super(ContainerType.File);
    }

    public SourceFile(String filepath) {
        this();
        this.filepath = filepath;
        name = FilenameUtils.getName(filepath);
        String dir = filepath.substring(0, filepath.length() - name.length());
        directoryPath = dir.length() > 0 ? dir.substring(0, dir.length() - 1) : dir;
    }

    public String getFilepath() {
        return filepath;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Returns the filePath
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public double normalizedSourceFolderDistance(ISourceFile c) {
        String s1 = directoryPath.toLowerCase();
        String s2 = c.getDirectoryPath().toLowerCase();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
