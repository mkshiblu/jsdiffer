package io.rminer.core.entities;

/**
 * A container type declaration such as a function declaration or a class declaration
 */
public abstract class DeclarationContainer extends Container {
    public DeclarationContainer() {
        super(ContainerType.Declaration);
    }
}
