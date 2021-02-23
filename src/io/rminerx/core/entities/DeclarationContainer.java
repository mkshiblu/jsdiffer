package io.rminerx.core.entities;

import io.rminerx.core.api.IDeclarationContainer;

/**
 * A container type declaration such as a function declaration or a class declaration
 */
public abstract class DeclarationContainer extends Container implements IDeclarationContainer {
//    /**
//     * Qualified name excluding the filename but including the parent function name.
//     * For example if function y() is declared inside x(), it will return x.y.
//     */
//    protected String qualifiedName;

    protected String parentContainerQualifiedName;

    public DeclarationContainer() {
        super(ContainerType.Declaration);
    }

//    /**
//     * Qualified name excluding the filename but including the parent function name.
//     * For example if function y() is declared inside x(), it will return x.y.
//     */
//    public String getQualifiedName() {
//        return qualifiedName;
//    }

    //public void setQualifiedName(String qualifiedName) {
      //  this.qualifiedName = qualifiedName;
   // }

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
}
