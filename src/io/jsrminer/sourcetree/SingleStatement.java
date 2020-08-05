package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

public class SingleStatement extends Statement {
    // private List<AbstractExpression> expressionList;
    //private List<VariableDeclaration> variableDeclarations;

    public SingleStatement() {
        // TODO find types
    }

    // TODO Move to factory or convert to member method?
    public static SingleStatement fromJson(String statement) {
        SingleStatement singleStatement = new SingleStatement();
        Any any = JsonIterator.deserialize(statement);

        // Text
        singleStatement.text = any.toString("text");

        // Type
        String type = any.toString("type");
        if (CodeElementType.IF_STATEMENT == type) {
            singleStatement.type = CodeElementType.IF_STATEMENT;
        }
        return singleStatement;
    }
}
