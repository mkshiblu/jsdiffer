package io.rminerx.core.api;


import org.eclipse.jgit.annotations.NonNull;

public interface IDeclarationContainer extends IContainer {
    /**
     * Returns the qualified name (i.e. using dots to prefix its name by joining parent names)
     */
    @NonNull
     String getQualifiedName();

    /**
     * Returns the qualified name of the parent container
     * @return
     */
     String getParentContainerQualifiedName();

    boolean isTopLevel();
}
