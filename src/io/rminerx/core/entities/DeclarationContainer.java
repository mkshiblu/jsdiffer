package io.rminerx.core.entities;

import io.rminerx.core.api.IDeclarationContainer;

/**
 * A container type declaration such as a function declaration or a class declaration
 */
public abstract class DeclarationContainer extends Container implements IDeclarationContainer {

    /**
     * Stores whether this function is a 'Top-Level' i.e. declared directly inside a
     * file and not nested
     */
    private boolean isTopLevel;

    /**
     * Fully Qualified name including the filename, parent function name if any.
     * For example if function y() is declared inside x() in file f.js, it will return f.x.y.
     */
    private String fullyQualifiedName;
    protected String parentContainerQualifiedName;
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public DeclarationContainer() {
        super(ContainerType.Declaration);
    }

    /**
     * Returns the container Qualified name under which it was declared
     *
     * @return
     */
    @Override
    public String getParentContainerQualifiedName() {
        return parentContainerQualifiedName;
    }

    public void setParentContainerQualifiedName(String parentContainerQualifiedName) {
        this.parentContainerQualifiedName = parentContainerQualifiedName;
    }

    public void setIsTopLevel(boolean isTopLevel) {
        this.isTopLevel = isTopLevel;
    }

    public boolean isTopLevel() {
        return isTopLevel;
    }
}
