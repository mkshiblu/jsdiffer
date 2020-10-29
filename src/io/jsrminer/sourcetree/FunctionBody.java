package io.jsrminer.sourcetree;

import io.jsrminer.parser.JsonCompositeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionBody extends CodeEntity {
    public final BlockStatement blockStatement;

    public FunctionBody(String functionBody) {
        blockStatement = JsonCompositeFactory.createBlockStatement(functionBody);
    }

    public List<OperationInvocation> getAllOperationInvocations() {
        java.util.List<OperationInvocation> invocations = new ArrayList<>();
        Map<String, List<OperationInvocation>> invocationMap = blockStatement.getAllMethodInvocationsIncludingNested();
        for (String key : invocationMap.keySet()) {
            invocations.addAll(invocationMap.get(key));
        }
        return invocations;
    }
}

