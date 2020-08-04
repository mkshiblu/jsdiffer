package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;

import java.io.IOException;

public class FunctionBody extends CodeElement {
    public FunctionBody(String functionBody) {

        BlockStatement body = BlockStatement.fromJson(functionBody);
    }
}

