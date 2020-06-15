package io.jsrminer.sourcetree;

public class SourceLocation {
    /**
     * File where the code element resides
     */
    private String file;

    public final int startLine;
    public final int startColumn;
    public final int endLine;
    public final int endColumn;

    public SourceLocation(String file, int startLine, int startColumn, int endLine, int endColumn) {
        this(startLine, startColumn, endLine, endColumn);
        this.file = file;
    }

    public SourceLocation(int startLine, int startColumn, int endLine, int endColumn) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
