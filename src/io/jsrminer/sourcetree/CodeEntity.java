package io.jsrminer.sourcetree;

/**
 * A complete entity such as functionDeclarations, VariableDeclarations etc.
 */
public class CodeEntity {
    protected SourceLocation sourceLocation;
    protected String text;
    protected CodeElementType type;

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CodeElementType getCodeElementType() {
        return type;
    }

    public void setType(CodeElementType type) {
        this.type = type;
    }

    public boolean equalsSourceLocation(CodeEntity test) {
        if (this.sourceLocation.getFilePath() == null) {
            if (test.sourceLocation.getFilePath() != null)
                return false;
        } else if (!this.sourceLocation.getFilePath().equals(test.sourceLocation.getFilePath()))
            return false;

        return this.sourceLocation.equalsLineAndColumn(test.sourceLocation);
    }
}
