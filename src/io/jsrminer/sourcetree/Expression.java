package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Expression extends CodeFragment {
    private String[] variables;
    private List<VariableDeclaration> variableDeclarations = new ArrayList<>();
    private String[] numericLiterals;
    private String[] infixOperators;
    private Map<String, List<OperationInvocation>> methodInvocationMap = new LinkedHashMap<>();
    private Map<String, List<ObjectCreation>> creationMap = new LinkedHashMap<>();

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

    public void setVariables(String[] variables) {
        this.variables = variables;
    }

    public String[] getNumericLiterals() {
        return numericLiterals;
    }

    public void setNumericLiterals(String[] numericLiterals) {
        this.numericLiterals = numericLiterals;
    }

    public String[] getInfixOperators() {
        return infixOperators;
    }

    public void setInfixOperators(String[] infixOperators) {
        this.infixOperators = infixOperators;
    }

    public String[] getVariables() {
        return variables;
    }

    public List<VariableDeclaration> getVariableDeclarations() {
        return variableDeclarations;
    }

    public Map<String, List<OperationInvocation>> getMethodInvocationMap() {
        return methodInvocationMap;
    }

    public void setMethodInvocationMap(Map<String, List<OperationInvocation>> methodInvocationMap) {
        this.methodInvocationMap = methodInvocationMap;
    }

    public Map<String, List<ObjectCreation>> getCreationMap() {
        return creationMap;
    }

    public void setCreationMap(Map<String, List<ObjectCreation>> creationMap) {
        this.creationMap = creationMap;
    }
}
