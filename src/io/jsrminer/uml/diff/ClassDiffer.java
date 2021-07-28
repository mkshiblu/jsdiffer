package io.jsrminer.uml.diff;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.refactorings.InlineOperationRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.ClassUtil;
import io.jsrminer.uml.FunctionUtil;
import io.jsrminer.uml.UMLAttribute;
import io.jsrminer.uml.diff.*;
import io.jsrminer.uml.diff.detection.ExtractOperationDetection;
import io.jsrminer.uml.diff.detection.InlineOperationDetection;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class ClassDiffer extends ContainerDiffer<IClassDeclaration, ClassDiff> {
    ClassDiff classDiff;

    public ClassDiffer(IClassDeclaration class1, IClassDeclaration class2) {
        super(new ClassDiff(class1, class2));
        this.classDiff = super.containerDiff;
    }

    @Override
    public ClassDiff diff() {
        super.reportAddedAndRemovedOperations();
        super.createBodyMapperForCommonFunctions();

        // processAnonymousFunctions(sourceDiff);
        super.checkForOperationSignatureChanges();

        processAttributes();
        checkForAttributeChanges();

        super.checkForInlinedOperations();
        super.checkForExtractedOperations();

        return this.containerDiff;
    }

    protected void processAttributes() {
        var originalClass = classDiff.getContainer1();
        var nextClass = classDiff.getContainer2();

        for (UMLAttribute attribute : originalClass.getAttributes()) {
            var matchingAttribute = ClassUtil.containsAttribute(nextClass, attribute);
            if (matchingAttribute == null) {
                this.classDiff.reportRemovedAttribute(attribute);
            } else {
                var attributeDiff = new UMLAttributeDiff(attribute, matchingAttribute/*, this, modelDiff*/);
                if (!attributeDiff.isEmpty()) {
                    this.classDiff.getRefactoringsBeforePostProcessing().addAll(attributeDiff.getRefactorings());
                    this.classDiff.getAttributeDiffList().add(attributeDiff);
                }
            }
        }
        for (UMLAttribute attribute : nextClass.getAttributes()) {
            UMLAttribute matchingAttribute = ClassUtil.containsAttribute(originalClass, attribute);
            if (matchingAttribute == null) {
                this.classDiff.reportAddedAttribute(attribute);
            } else {
                var attributeDiff = new UMLAttributeDiff(matchingAttribute, attribute/*, this, modelDiff*/);
                if (!attributeDiff.isEmpty()) {
                    this.classDiff.getRefactoringsBeforePostProcessing().addAll(attributeDiff.getRefactorings());
                    this.classDiff.getAttributeDiffList().add(attributeDiff);
                }
            }
        }
    }

    protected void checkForAttributeChanges() throws RefactoringMinerTimedOutException {
        for (Iterator<UMLAttribute> removedAttributeIterator = classDiff.getRemovedAttributes().iterator(); removedAttributeIterator.hasNext(); ) {
            UMLAttribute removedAttribute = removedAttributeIterator.next();
            for (Iterator<UMLAttribute> addedAttributeIterator = classDiff.getAddedAttributes().iterator(); addedAttributeIterator.hasNext(); ) {
                UMLAttribute addedAttribute = addedAttributeIterator.next();
                if (removedAttribute.getName().equals(addedAttribute.getName())) {
                    UMLAttributeDiff attributeDiff = new UMLAttributeDiff(removedAttribute, addedAttribute/*, this, modelDiff*/);
                    this.classDiff.getRefactoringsBeforePostProcessing().addAll(attributeDiff.getRefactorings());
                    addedAttributeIterator.remove();
                    removedAttributeIterator.remove();
                    this.classDiff.getAttributeDiffList().add(attributeDiff);
                    break;
                }
            }
        }
    }
}
