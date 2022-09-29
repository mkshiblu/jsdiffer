package io.jsrminer.sourcetree;

import io.rminerx.core.api.IAnonymousFunctionDeclaration;

/**
 * An anonymous function which is typically used in a single statement or expression
 */
public class AnonymousFunctionDeclaration extends FunctionDeclaration implements IAnonymousFunctionDeclaration {
    private String text;
    private String optionalName;

    public void setText(String text) {
        this.text = text;
    }

    public AnonymousFunctionDeclaration() {
    }

    @Override
    public String getText() {
        return text;
    }

    public String getOptionalName() {
        return optionalName;
    }

    public void setOptionalName(String optionalName) {
        this.optionalName = optionalName;
    }
}
