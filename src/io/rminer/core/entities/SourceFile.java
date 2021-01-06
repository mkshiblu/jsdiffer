package io.rminer.core.entities;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.rminer.core.api.IComposite;
import io.rminer.core.api.ISourceFile;

/**
 * Represents a Source File model
 */
public class SourceFile extends Container implements ISourceFile {
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

    @Override
    public FunctionDeclaration[] getFunctionDeclarations() {
        if (this.body == null)
            throw new NullPointerException("SourceFile's body is null that means not populated correctly");

        //this.body.getFunctionDeclarations();
        return null;
    }
}
