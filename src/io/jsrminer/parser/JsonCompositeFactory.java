package io.jsrminer.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.sourcetree.*;

import java.util.*;

public class JsonCompositeFactory {

    private static void populateInvocationProperties(Any invocationAny, Invocation invocation) {
        invocation.setText(invocationAny.toString("text"));

        // Type
        String type = invocationAny.toString("type");
        invocation.setType(CodeElementType.getFromTitleCase(type));

//        //Loc
//        invocation.setSourceLocation(invocationAny.get("loc").as(SourceLocation.class));
    }

    public static ObjectCreation createObjectCreation(Any any) {
        ObjectCreation creation = new ObjectCreation();
        populateInvocationProperties(any, creation);
        creation.setTypeName(any.toString("typeName"));

        if (any.keys().contains("expressionText")) {
            creation.setExpressionText(any.toString("expressionText"));
        }
        return creation;
    }

    public static OperationInvocation createOperationInvocation(Any any) {
        OperationInvocation operationInvocation = new OperationInvocation();
        populateInvocationProperties(any, operationInvocation);

        operationInvocation.setFunctionName(any.toString("functionName"));

        if (any.keys().contains("expressionText")) {
            operationInvocation.setExpression(any.toString("expressionText"));
        }

        return operationInvocation;
    }

    /**
     * @param any SingleStatement any node
     */
    public static SingleStatement createSingleStatement(Any any) {
        SingleStatement singleStatement = new SingleStatement();
        // Text
        singleStatement.setText(any.toString("text"));

        // Type
        String type = any.toString("type");
        singleStatement.setType(CodeElementType.getFromTitleCase(type));

        //Loc
        singleStatement.setSourceLocation(any.get("loc").as(SourceLocation.class));

        // Additional info if present
        // region
        // Identifiers
        if (any.keys().contains("identifiers")) {
            Set<String> variables = any.get("identifiers").as(singleStatement.getVariables().getClass());
            singleStatement.setVariables(variables);
        }
        //endregion

        // region Variable declarations
        if (any.keys().contains("variableDeclarations")) {
            Set<VariableDeclaration> vds = singleStatement.getVariableDeclarations();
            List<Any> vdAnys = any.get("variableDeclarations").asList();
            vdAnys.forEach((variableDeclarationAny -> {
                vds.add(createVariableDeclaration(variableDeclarationAny));
            }));
        }
        // endregion

        // region Function Invocation
        if (any.keys().contains("functionInvocations")) {
            parseAndLoadFunctionInvocations(any.get("functionInvocations"), singleStatement.getMethodInvocationMap());
        }
        // endregion

        // region Object Creations
        if (any.keys().contains("objectCreations")) {
            parseAndLoadObjectCreations(any.get("objectCreations"), singleStatement.getCreationMap());
        }
        // endregion

        return singleStatement;
    }

    public static void parseAndLoadFunctionInvocations(Any functionInvocationsAny, Map<String, List<OperationInvocation>> operationInvocationMap) {
        for (Any operationInvocationAny : functionInvocationsAny.asList()) {
            OperationInvocation operationInvocation = createOperationInvocation(operationInvocationAny);
            final String text = operationInvocationAny.toString("text");
            List<OperationInvocation> list = operationInvocationMap.get(text);
            if (list == null) {
                list = new ArrayList<>();
                operationInvocationMap.put(text, list);
            }
            list.add(operationInvocation);
        }
    }


    public static void parseAndLoadObjectCreations(Any creationsAny, Map<String, List<ObjectCreation>> creationMap) {
        for (Any creationAny : creationsAny.asList()) {
            ObjectCreation creation = createObjectCreation(creationAny);
            final String text = creationAny.toString("text");
            List<ObjectCreation> list = creationMap.get(text);
            if (list == null) {
                list = new ArrayList<>();
                creationMap.put(text, list);
            }
            list.add(creation);
        }
    }

    public static SingleStatement createSingleStatement(String singleStatementJson) {
        return createSingleStatement(JsonIterator.deserialize(singleStatementJson));
    }

    public static VariableDeclaration createVariableDeclaration(Any any) {
        VariableDeclaration vd = new VariableDeclaration(any.toString("variableName"));
        vd.setText(any.toString("text"));
        VariableDeclarationKind kind = VariableDeclarationKind.fromName(any.toString("kind"));
        vd.setKind(kind);

        if (any.keys().contains("initializer")) {
            Expression expression = createExpression(any.get("initializer"));
            vd.setInitializer(expression);
        }
        return vd;
    }

    public static Expression createExpression(Any any) {
        Expression expression = new Expression();
        //Text
        expression.setText(any.toString("text"));

        // Info
        expression.setVariables(any.get("identifiers").as(String[].class));
        expression.setNumericLiterals(any.get("numericLiterals").as(String[].class));
        expression.setInfixOperators(any.get("infixOperators").as(String[].class));

        final List<Any> anys = any.get("variableDeclarations").asList();

        // Vds
        for (Any variableDeclarationAny : anys) {
            VariableDeclaration declaration = new VariableDeclaration(variableDeclarationAny.toString("name"));
            expression.getVariableDeclarations().add(declaration);
        }

        // Additional info if present
        // region
        // Identifiers
        if (any.keys().contains("identifiers")) {
            String[] variables = any.get("identifiers").as(expression.getVariables().getClass());
            expression.setVariables(variables);
        }
        //endregion
        parseAndLoadFunctionInvocations(any.get("functionInvocations"), expression.getMethodInvocationMap());
        parseAndLoadObjectCreations(any.get("objectCreations"), expression.getCreationMap());

        return expression;
    }
}
