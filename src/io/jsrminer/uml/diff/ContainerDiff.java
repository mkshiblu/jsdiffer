package io.jsrminer.uml.diff;

import io.rminerx.core.entities.Container;

public class ContainerDiff {
    Container container1;
    Container container2;

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
}
