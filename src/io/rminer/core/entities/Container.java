package io.rminer.core.entities;

import io.rminer.core.api.IComposite;
import io.rminer.core.api.IContainer;

public abstract class Container implements IContainer {
    protected ContainerType containerType;
    protected IComposite body;

    @Override
    public ContainerType getContainerType() {
        return containerType;
    }

    /**
     * Returns the body of the container which is a Block Statement
     * Even though file that does not have a direct block statement it is assumed to
     * have Wrapper Block statement
     *
     * @return
     */
    @Override
    public IComposite getBody() {
        return body;
    }
}
