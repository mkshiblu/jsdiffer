package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.OperationInvocation;

public class VariableReplacementWithMethodInvocation extends Replacement {
    private OperationInvocation invokedOperation;
    private Direction direction;

    public VariableReplacementWithMethodInvocation(String before, String after, OperationInvocation invokedOperation, Direction direction) {
        super(before, after, ReplacementType.VARIABLE_REPLACED_WITH_METHOD_INVOCATION);
        this.invokedOperation = invokedOperation;
        this.direction = direction;
    }

    public OperationInvocation getInvokedOperation() {
        return invokedOperation;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        VARIABLE_TO_INVOCATION, INVOCATION_TO_VARIABLE;
    }
}
