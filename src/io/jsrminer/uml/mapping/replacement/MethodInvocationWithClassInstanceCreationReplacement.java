package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.ObjectCreation;
import io.jsrminer.sourcetree.OperationInvocation;

public class MethodInvocationWithClassInstanceCreationReplacement extends Replacement {
    private OperationInvocation invokedOperationBefore;
    private ObjectCreation objectCreationAfter;

    public MethodInvocationWithClassInstanceCreationReplacement(String before, String after, ReplacementType type,
                                                                OperationInvocation invokedOperationBefore, ObjectCreation objectCreationAfter) {
        super(before, after, type);
        this.invokedOperationBefore = invokedOperationBefore;
        this.objectCreationAfter = objectCreationAfter;
    }

    public OperationInvocation getInvokedOperationBefore() {
        return invokedOperationBefore;
    }

    public ObjectCreation getObjectCreationAfter() {
        return objectCreationAfter;
    }

}