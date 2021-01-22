package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.ObjectCreation;
import io.jsrminer.sourcetree.OperationInvocation;

public class ClassInstanceCreationWithMethodInvocationReplacement extends Replacement {
    private ObjectCreation objectCreationBefore;
    private OperationInvocation invokedOperationAfter;

    public ClassInstanceCreationWithMethodInvocationReplacement(String before, String after, ReplacementType type,
                                                                ObjectCreation objectCreationBefore, OperationInvocation invokedOperationAfter) {
        super(before, after, type);
        this.objectCreationBefore = objectCreationBefore;
        this.invokedOperationAfter = invokedOperationAfter;
    }

    public ObjectCreation getObjectCreationBefore() {
        return objectCreationBefore;
    }

    public OperationInvocation getInvokedOperationAfter() {
        return invokedOperationAfter;
    }

}
