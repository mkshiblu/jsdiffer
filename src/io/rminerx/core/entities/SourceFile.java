package io.rminerx.core.entities;

import io.jsrminer.uml.diff.StringDistance;
import io.rminerx.core.api.ISourceFile;
import org.apache.commons.io.FilenameUtils;

/**
 * Represents a Source File model
 */
public class SourceFile extends Container implements ISourceFile {

    private String filepath;
    private String name;

    private String directoryName;
    private String directoryPath;

    public SourceFile(String filepath) {
        super(ContainerType.File);
        this.filepath = filepath;
        name = FilenameUtils.getName(filepath);
        qualifiedName = name;
        String dir = filepath.substring(0, filepath.length() - name.length());
        directoryPath = dir.length() > 0 ? dir.substring(0, dir.length() - 1) : dir;
        directoryName = FilenameUtils.getName(directoryPath);
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

    public String getDirectoryPath() {
        return directoryPath;
    }

    public String getDirectoryName() {
        return directoryName;
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
        return getFilepath();
    }

    @Override
    public String getFullyQualifiedName() {
        return getFilepath();
    }
}
