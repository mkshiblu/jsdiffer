package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.rminerx.core.entities.Container;

import java.util.ArrayList;
import java.util.List;

public class ContainerDiff {
    Container container1;
    Container container2;

    private final List<FunctionDeclaration> addedOperations = new ArrayList<>();
    private final List<FunctionDeclaration> removedOperations = new ArrayList<>();

    public ContainerDiff(Container container1, Container container2) {
        this.container1 = container1;
        this.container2 = container2;
    }

    public Container getContainer1() {
        return container1;
    }

    public Container getContainer2() {
        return container2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation){
        this.addedOperations.add(addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation){
        this.removedOperations.add(removedOperation);
    }

    public List<FunctionDeclaration> getAddedOperations() {
        return addedOperations;
    }

    public List<FunctionDeclaration> getRemovedOperations() {
        return removedOperations;
    }
}
