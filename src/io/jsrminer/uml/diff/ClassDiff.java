package io.jsrminer.uml.diff;

import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IContainer;

public class ClassDiff  extends ContainerDiff{
    public ClassDiff(IClassDeclaration class1, IClassDeclaration class2) {
        super(class1, class2);
    }
}
