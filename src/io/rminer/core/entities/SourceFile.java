package io.rminer.core.entities;

import io.rminer.core.api.ISourceFile;

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

    public SourceFile() {
        super(ContainerType.File);
    }

    public SourceFile(String filepath) {
        this();
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }
}
