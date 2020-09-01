package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import java.util.ArrayList;
import java.util.List;

public class Expression extends CodeFragment {
    private String[] variables;
    private List<VariableDeclaration> variableDeclarations;
    private String[] numericLiterals;
    private String[] infixOperators;

    public static Expression fromJSON(final String jsonExpression) {
        Expression expression = new Expression();
        Any any = JsonIterator.deserialize(jsonExpression);

        //Text
        expression.text = any.toString("text");

        // Info
        expression.variables = any.get("identifiers").as(String[].class);
        expression.numericLiterals = any.get("numericLiterals").as(String[].class);
        expression.infixOperators = any.get("infixOperators").as(String[].class);

        final List<Any> anys = any.get("variableDeclarations").asList();
        expression.variableDeclarations = new ArrayList<>();

        for (Any variableDeclarationAny : anys) {
            VariableDeclaration declaration = new VariableDeclaration(variableDeclarationAny.toString("name"));
            expression.variableDeclarations.add(declaration);
        }

        return expression;
    }
}
