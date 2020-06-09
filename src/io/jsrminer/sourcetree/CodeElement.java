package io.jsrminer.sourcetree;

/**
 * Base class for all the code elements
 */
public abstract class CodeElement {
    private SourceLocation location;

    public CodeElement() {
    }

    public CodeElement(SourceLocation location) {
        this.location = location;
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
}
