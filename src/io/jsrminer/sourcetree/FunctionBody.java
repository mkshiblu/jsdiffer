package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;

import java.io.IOException;

public class FunctionBody extends BlockStatement {
    public FunctionBody(String functionBody) throws IOException {

        JsonIterator itr = JsonIterator.parse(functionBody);

        for (String field = itr.readObject(); field != null; field = itr.readObject()) {
            switch (field) {
                case "statements":
                    //this.statements = itr.readArray();
                    continue;
                default:
                    break;
            }
        }
    }
}

