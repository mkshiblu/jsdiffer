package io.jsrminer.uml.diff;

import io.jsrminer.uml.UMLAttribute;
import io.rminerx.core.api.IClassDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ClassDiff extends ContainerDiff {

    protected List<UMLAttribute> addedAttributes = new ArrayList<>();
    protected List<UMLAttribute> removedAttributes = new ArrayList<>();
    protected List<UMLAttributeDiff> attributeDiffList = new ArrayList<>();

    public ClassDiff(IClassDeclaration class1, IClassDeclaration class2) {
        super(class1, class2);
    }

    private boolean staticChanged;

    public boolean isEmpty() {
        return addedOperations.isEmpty() && removedOperations.isEmpty() &&
                addedAttributes.isEmpty() && removedAttributes.isEmpty() &&
                //addedEnumConstants.isEmpty() && removedEnumConstants.isEmpty() &&
                operationDiffList.isEmpty() && attributeDiffList.isEmpty() &&
                operationBodyMapperList.isEmpty()
                //&& enumConstantDiffList.isEmpty()
                //&& !visibilityChanged && !abstractionChanged && !finalChanged
                && !staticChanged;
    }

    public void setStaticChanged(boolean staticChanged) {
        this.staticChanged = staticChanged;
    }

    public boolean getStaticChanged() {
        return this.staticChanged;
    }
}
