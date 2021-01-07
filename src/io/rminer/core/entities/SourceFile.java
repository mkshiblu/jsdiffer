package io.rminer.core.entities;

import io.rminer.core.api.ISourceFile;

/**
 * Represents a Source File model
 */
public class SourceFile extends Container implements ISourceFile {
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
}
