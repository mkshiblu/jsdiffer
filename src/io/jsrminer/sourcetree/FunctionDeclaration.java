package io.jsrminer.sourcetree;

public class FunctionDeclaration extends CodeElement {
    private String[] parameters;

    /**
     * The name of the function.
     */
    public final String name;

    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    public final String qualifiedName;

    public final String namespace;

    /**
     * Fully Qualified name including the filename, parent function name if any.
     * For example if function y() is declared inside x() in file f.js, it will return f.x.y.
     */
    private String fullyQualifiedName;

    /**
     * Holds the body of the function
     */
    private FunctionBody body;

    /**
     * Stores whether the body of the function is empty or not
     */
    private boolean isEmptyBody;

    /**
     * Stores whether this function is a 'Top-Level' i.e. declared directly inside a
     * file and not nested
     */
    public final boolean isTopLevel;

    public FunctionDeclaration(String qualifiedName, boolean isTopLevel) {
        this.isTopLevel = isTopLevel;
        this.qualifiedName = qualifiedName;
        int idx = qualifiedName.lastIndexOf('.');
        if (idx != -1) {
            name = qualifiedName.substring(idx + 1);
            namespace = qualifiedName.substring(0, idx);
        } else {
            name = qualifiedName;
            namespace = null;
        }
    }

    @Override
    public String toString() {
        return qualifiedName;
    }

    @Override
    public void setSourceLocation(SourceLocation sourceLocation) {
        super.setSourceLocation(sourceLocation);
        fullyQualifiedName = sourceLocation.getFile() + "|" + qualifiedName;
    }

    public boolean hasIdenticalBody(FunctionDeclaration fd) {
        return this.body.equals(fd.body);
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    // region Setters & getters
    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public void setBody(FunctionBody body) {
        this.body = body;
    }

    public FunctionBody getBody() {
        return body;
    }

    public void setIsEmptyBody(boolean isEmptyBody) {
        this.isEmptyBody = isEmptyBody;
    }
    //endregion
}
