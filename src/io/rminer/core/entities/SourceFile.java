package io.rminer.core.entities;

import io.rminer.core.api.IComposite;

/**
 * Represents a Source File model
 */
public class SourceFile extends Container {
    private String filepath;

    public SourceFile(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Sets the content of the source file as a composite body
     */
    public void setBody(IComposite body) {
        this.body = body;
    }

    @Override
    public IComposite getBody() {
        return null;
    }
}
