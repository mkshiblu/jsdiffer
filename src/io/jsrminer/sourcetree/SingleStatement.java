package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

public class SingleStatement extends Statement {
    // private List<AbstractExpression> expressionList;
    //private List<VariableDeclaration> variableDeclarations;

    public SingleStatement() {
        // TODO find types
    }

    // TODO Move to factory
    public static SingleStatement fromJson(String singleStatementJson) {
        SingleStatement singleStatement = new SingleStatement();
        Any any = JsonIterator.deserialize(singleStatementJson);

        // Text
        singleStatement.text = any.toString("text");

        // Type
        String type = any.toString("type");
        singleStatement.type = CodeElementType.getFromTitleCase(type);

        //Loc
        singleStatement.sourceLocation = any.get("loc").as(SourceLocation.class);

        return singleStatement;
    }

    @Override
    public String toString() {
        return text;
    }
}
