package io.jsrminer.sourcetree;

/**
 * Base class for all the code elements
 */
public abstract class CodeFragment {
    protected SourceLocation sourceLocation;
    protected String text;
    protected CodeElementType type;

    public CodeFragment() {
    }

    public CodeFragment(String text) {
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

    public void setType(CodeElementType type) {
        this.type = type;
    }

    /**
     * Returns the Code Element type of this fragment
     */
    public CodeElementType getType() {
        return type;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean equalsSourceLocation(CodeFragment test) {
        if (this.getFile() == null) {
            if (test.getFile() != null)
                return false;
        } else if (!this.getFile().equals(test.getFile()))
            return false;

        return this.sourceLocation.equalsLineAndColumn(test.sourceLocation);
    }
}
