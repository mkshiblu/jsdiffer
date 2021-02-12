package io.jsrminer.sourcetree;

import io.rminerx.core.api.IAnonymousClassDeclaration;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.entities.DeclarationContainer;
import org.apache.commons.lang3.NotImplementedException;

public class AnonymousClassDeclaration extends DeclarationContainer implements IAnonymousClassDeclaration, IClassDeclaration {

    @Override
    public String getFullyQualifiedName() {
        throw new NotImplementedException();
    }
}
