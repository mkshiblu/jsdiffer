package io.jsrminer.sourcetree;

/**
 * Base class for all the code elements
 */
public abstract class CodeElement {
    protected SourceLocation sourceLocation;
    protected String text;

    public CodeElement() {
    }

    public CodeElement(String text) {
        this.text = text;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getFile() {
        return sourceLocation.getFile();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
