package io.jsrminer.uml.diff;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.rminer.core.api.IContainer;
import io.rminer.core.api.ISourceFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a diff between two containers
 */
public class ContainerDiff {
    public final IContainer container1;
    public final IContainer cotainer2;
    private List<UMLOperationDiff> operationDiffList = new ArrayList<>();
    public final List<IRefactoring> refactorings = new ArrayList<>();

    /**
     * Name map
     */
    private final Map<String, FunctionDeclaration> addedOperations = new LinkedHashMap<>();
    private final Map<String, FunctionDeclaration> removedOperations = new LinkedHashMap<>();

    public ContainerDiff(ISourceFile container1, ISourceFile cotainer2) {
        this.container1 = container1;
        this.cotainer2 = cotainer2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation) {
        if (addedOperations.containsKey(addedOperation.getName()))
            throw new RuntimeException("Duplicate names for removed operations" + addedOperation);
        addedOperations.put(addedOperation.getName(), addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation) {
        if (removedOperations.containsKey(removedOperation.getName()))
            throw new RuntimeException("Duplicate names for removed operations" + removedOperation);
        removedOperations.put(removedOperation.getName(), removedOperation);
    }

    public boolean isRemovedOperation(String functionName) {
        return this.removedOperations.containsKey(functionName);
    }

    public Map<String, FunctionDeclaration> getAddedOperations() {
        return addedOperations;
    }

    public Map<String, FunctionDeclaration> getRemovedOperations() {
        return removedOperations;
    }

    public List<UMLOperationDiff> getOperationDiffList() {
        return operationDiffList;
    }

    public UMLOperationDiff getOperationDiff(FunctionDeclaration operation1, FunctionDeclaration operation2) {
        for (UMLOperationDiff diff : operationDiffList) {
            if (diff.function1.equals(operation1) && diff.function2.equals(operation2)) {
                return diff;
            }
        }
        return null;
    }
}
