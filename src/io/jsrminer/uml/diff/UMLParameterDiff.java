package io.jsrminer.uml.diff;

import io.jsrminer.refactorings.IRefactoring;
import io.jsrminer.uml.UMLParameter;

import java.util.LinkedHashSet;
import java.util.Set;

public class UMLParameterDiff extends Diff {

    final boolean isNameChanged;
    final boolean defaultValueChanged;
    final UMLParameter parameter1;
    final UMLParameter parameter2;

    public UMLParameterDiff(UMLParameter parameter1, UMLParameter parameter2) {
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
        isNameChanged = !parameter1.name.equals(parameter2.name);
        defaultValueChanged = !parameter1.hasSameDefaultValue(parameter2);
        // TODO handle default value since it could expression and other function calls or a huge function expression
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("parameter ")
                .append(parameter1)
                .append(':')
                .append(System.lineSeparator());
        if (isNameChanged) {
            sb.append("name changed from ")
                    .append(parameter1.name)
                    .append(" to ")
                    .append(parameter2.name)
                    .append(System.lineSeparator());
        }

        return sb.toString();
    }

    public Set<IRefactoring> getRefactorings() {
        Set<IRefactoring> refactorings = new LinkedHashSet<>();
//        VariableDeclaration originalVariable = this.parameter1.getVariableDeclaration();
//        VariableDeclaration newVariable = parameter1().getVariableDeclaration();
//        Set<CodeFragmentMapping> references = VariableReferenceExtractor.findReferences(originalVariable, newVariable, mappings);
//        RenameVariableRefactoring renameRefactoring = null;
//        if (isNameChanged() && !inconsistentReplacement(originalVariable, newVariable)) {
//            renameRefactoring = new RenameVariableRefactoring(originalVariable, newVariable, removedOperation, addedOperation, references);
//            refactorings.add(renameRefactoring);
//        }
//        if ((isTypeChanged() || isQualifiedTypeChanged()) && !inconsistentReplacement(originalVariable, newVariable)) {
//            ChangeVariableTypeRefactoring refactoring = new ChangeVariableTypeRefactoring(originalVariable, newVariable, removedOperation, addedOperation, references);
//            if (renameRefactoring != null) {
//                refactoring.addRelatedRefactoring(renameRefactoring);
//            }
//            refactorings.add(refactoring);
//        }
//        for (UMLAnnotation annotation : annotationListDiff.getAddedAnnotations()) {
//            AddVariableAnnotationRefactoring refactoring = new AddVariableAnnotationRefactoring(annotation, originalVariable, newVariable, removedOperation, addedOperation);
//            refactorings.add(refactoring);
//        }
//        for (UMLAnnotation annotation : annotationListDiff.getRemovedAnnotations()) {
//            RemoveVariableAnnotationRefactoring refactoring = new RemoveVariableAnnotationRefactoring(annotation, originalVariable, newVariable, removedOperation, addedOperation);
//            refactorings.add(refactoring);
//        }
//        for (UMLAnnotationDiff annotationDiff : annotationListDiff.getAnnotationDiffList()) {
//            ModifyVariableAnnotationRefactoring refactoring = new ModifyVariableAnnotationRefactoring(annotationDiff.getRemovedAnnotation(), annotationDiff.getAddedAnnotation(), originalVariable, newVariable, removedOperation, addedOperation);
//            refactorings.add(refactoring);
//        }
        return refactorings;
    }
}
