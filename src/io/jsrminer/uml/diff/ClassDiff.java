package io.jsrminer.uml.diff;

import io.jsrminer.uml.UMLAttribute;
import io.rminerx.core.api.IClassDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ClassDiff extends ContainerDiff<IClassDeclaration> {

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

    public void reportRemovedAttribute(UMLAttribute attribute) {
        this.removedAttributes.add(attribute);
    }

    public void reportAddedAttribute(UMLAttribute attribute) {
        this.addedAttributes.add(attribute);
    }

    public List<UMLAttributeDiff> getAttributeDiffList() {
        return attributeDiffList;
    }

    public List<UMLAttribute> getAddedAttributes() {
        return addedAttributes;
    }

    public List<UMLAttribute> getRemovedAttributes() {
        return removedAttributes;
    }
}
