package io.jsrminer.sourcetree;

import io.jsrminer.parser.JsonCompositeFactory;

public class FunctionBody extends CodeEntity {
    public final BlockStatement blockStatement;

    public FunctionBody(String functionBody) {
        blockStatement = JsonCompositeFactory.createBlockStatement(functionBody);
    }
}

