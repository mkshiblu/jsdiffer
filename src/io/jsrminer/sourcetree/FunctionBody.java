package io.jsrminer.sourcetree;

public class FunctionBody extends CodeFragment {
    public final BlockStatement blockStatement;

    public FunctionBody(String functionBody) {
        blockStatement = BlockStatement.fromJson(functionBody);
    }
}

