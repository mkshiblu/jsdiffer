package io.jsrminer.sourcetree;

import io.rminerx.core.api.IAnonymousClassDeclaration;
import org.apache.commons.lang3.NotImplementedException;

public class AnonymousClassDeclaration extends ClassDeclaration implements IAnonymousClassDeclaration {
    private String text;

    public void setText(String text) {
        this.text = text;
    }
    @Override
    public String getFullyQualifiedName() {
        throw new NotImplementedException();
    }
}
