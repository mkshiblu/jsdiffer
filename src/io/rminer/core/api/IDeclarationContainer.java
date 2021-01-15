package io.rminer.core.api;


import org.eclipse.jgit.annotations.NonNull;

public interface IDeclarationContainer extends IContainer {
    /**
     * Returns the qualified name (i.e. using dots to prefix its name by joining parent names)
     */
    @NonNull
    public String getQualifiedName();

    /**
     * Returns the qualified name of the parent container
     * @return
     */
    public String getParentContainerQualifiedName();
}
