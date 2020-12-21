package io.rminer.core.entities;

import io.rminer.core.api.IComposite;
import io.rminer.core.api.IFunctionDeclaration;

import java.util.List;

public class FunctionDeclaration extends ContainerDeclaration implements IFunctionDeclaration {

    public List<String> getParameters() {
        return null;
    }

    @Override
    public IComposite getBody() {
        return null;
    }
}
