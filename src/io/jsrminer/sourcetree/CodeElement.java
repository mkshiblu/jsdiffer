package io.jsrminer.sourcetree;

/**
 * Base class for all the code elements
 */
public abstract class CodeElement {
    protected SourceLocation location;
    protected String text;

    public CodeElement() {
    }

    public CodeElement(String text) {
        this.text = text;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public void setLocation(SourceLocation location) {
        this.location = location;
    }

    public String getFile() {
        return location.getFile();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
