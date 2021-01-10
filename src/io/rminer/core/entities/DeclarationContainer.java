package io.rminer.core.entities;

/**
 * A container type declaration such as a function declaration or a class declaration
 */
public abstract class DeclarationContainer extends Container {
    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    protected String qualifiedName;

    public DeclarationContainer() {
        super(ContainerType.Declaration);
    }

    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

}
