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
    public final int start;
    public final int end;

    public SourceLocation() {
        this(0, 0, 0, 0, 0, 0);
    }

    public SourceLocation(String file, int startLine, int startColumn, int endLine, int endColumn, int start, int end) {
        this(startLine, startColumn, endLine, endColumn, start, end);
        this.file = file;
    }

    public SourceLocation(int startLine, int startColumn, int endLine, int endColumn, int start, int end) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.start = start;
        this.end = end;
    }

    public String getFilePath() {
        return file;
    }

    public boolean equalsLineAndColumn(SourceLocation testLocation) {
        return this.endColumn == testLocation.endColumn
                && this.startLine == testLocation.startLine
                && this.endLine == testLocation.endLine
                && this.startColumn == testLocation.startColumn;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return this.file + "(" + startLine + "," + startColumn + ")";
    }

    public boolean subsumes(SourceLocation other) {
        boolean isEqualFile = this.file != null && this.file == other.file;
        return isEqualFile &&
                this.start <= other.start &&
                this.end >= other.end;
    }

    public boolean equalSourceLocation(SourceLocation other) {
        if (this.getFilePath() == null) {
            if (other.getFilePath() != null)
                return false;
        } else if (!this.getFilePath().equals(other.getFilePath()))
            return false;

        return this.equalsLineAndColumn(other);
    }
}
