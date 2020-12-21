package io.rminer.core.entities;

import io.rminer.core.api.IClassDeclaration;
import io.rminer.core.api.IComposite;

public class ClassDeclaration extends ContainerDeclaration implements IClassDeclaration {
    @Override
    public IComposite getBody() {
        return null;
    }
}
