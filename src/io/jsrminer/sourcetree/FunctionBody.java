package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;

import java.io.IOException;

public class FunctionBody extends CodeElement {
    public final BlockStatement blockStatement;

    public FunctionBody(String functionBody) {
        blockStatement = BlockStatement.fromJson(functionBody);
    }
}

