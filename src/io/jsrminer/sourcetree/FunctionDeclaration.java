package io.jsrminer.sourcetree;

public class FunctionDeclaration extends CodeElement {
    private String[] parameters;

    /**
     * The name of the function.
     */
    private String name;

    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    private String qualifiedName;

    /**
     * Fully Qualified name including the filename, parent function name if any.
     * For example if function y() is declared inside x() in file f.js, it will return f.x.y.
     */
    private String fullyQualifiedName;

    public FunctionDeclaration() {

    }

    public FunctionDeclaration(String name) {
        this.name = name;
    }

    public void setParameters(String[] parameters) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
