package io.jsrminer.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.sourcetree.*;

import java.util.*;

public class CompositeJsonFactory {
    
    public static OperationInvocation createOperationInvocation(Any any) {
        OperationInvocation operationInvocation = new OperationInvocation();
        operationInvocation.setFunctionName(any.toString("functionName"));
        operationInvocation.setExpression(any.toString("expression"));
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
            extractFunctionInvocations(any.get("functionInvocations"), singleStatement.getMethodInvocationMap());
        }
        // endregion

        return singleStatement;
    }

    public static void extractFunctionInvocations(Any functionInvocationsAny, Map<String, List<OperationInvocation>> operationInvocationMap) {
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

    public static SingleStatement createSingleStatement(String singleStatementJson) {
        return createSingleStatement(JsonIterator.deserialize(singleStatementJson));
    }

    public static VariableDeclaration createVariableDeclaration(Any any) {
        VariableDeclaration vd = new VariableDeclaration(any.toString("variableName"));
        //vd.setText(any.toString("text"));
        VariableDeclarationKind kind = VariableDeclarationKind.fromName(any.toString("kind"));
        vd.setKind(kind);
        //Expression expression = Expression.fromJSON()
        return vd;
    }

//    public static Expression createExpression(Any any) {
//        Expression expression = new Expression();
//
//        //Text
//        expression.setText(any.toString("text"));
//
//        // Info
//        expression.variables = any.get("identifiers").as(String[].class);
//        expression.numericLiterals = any.get("numericLiterals").as(String[].class);
//        expression.infixOperators = any.get("infixOperators").as(String[].class);
//
//        final List<Any> anys = any.get("variableDeclarations").asList();
//        expression.variableDeclarations = new ArrayList<>();
//
//        for (Any variableDeclarationAny : anys) {
//            VariableDeclaration declaration = new VariableDeclaration(variableDeclarationAny.toString("name"));
//            expression.variableDeclarations.add(declaration);
//        }
//
//        return expression;
//    }
}
