package io.jsrminer.uml.diff;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.refactorings.CandidateAttributeRefactoring;
import io.jsrminer.refactorings.Refactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.UMLAttribute;
import io.jsrminer.uml.mapping.FunctionBodyMapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UMLAttributeDiff {
    private UMLAttribute removedAttribute;
    private UMLAttribute addedAttribute;
    private boolean visibilityChanged;
    private boolean typeChanged;
    private boolean qualifiedTypeChanged;
    private boolean renamed;
    private boolean staticChanged;
    private boolean finalChanged;
    private boolean volatileChanged;
    private boolean transientChanged;
    private List<FunctionBodyMapper> operationBodyMapperList;
    //private UMLAnnotationListDiff annotationListDiff;
    //private List<UMLAnonymousClassDiff> anonymousClassDiffList;
    private FunctionDeclaration addedGetter;
    private FunctionDeclaration addedSetter;

    public UMLAttributeDiff(UMLAttribute removedAttribute, UMLAttribute addedAttribute) {
        this.removedAttribute = removedAttribute;
        this.addedAttribute = addedAttribute;
    }

//    public UMLAttributeDiff(UMLAttribute removedAttribute, UMLAttribute addedAttribute
//            , UMLClassBaseDiff classDiff, UMLModelDiff modelDiff) throws RefactoringMinerTimedOutException {
//        this(removedAttribute, addedAttribute, classDiff.getOperationBodyMapperList());
//        List<UMLAnonymousClass> removedAttributeAnonymousClassList = removedAttribute.getAnonymousClassList();
//        List<UMLAnonymousClass> addedAttributeAnonymousClassList = addedAttribute.getAnonymousClassList();
//        if(removedAttributeAnonymousClassList.size() == addedAttributeAnonymousClassList.size() && removedAttributeAnonymousClassList.size() > 0) {
//            for(int i=0; i<removedAttributeAnonymousClassList.size(); i++) {
//                UMLAnonymousClass anonymousClass1 = removedAttributeAnonymousClassList.get(i);
//                UMLAnonymousClass anonymousClass2 = addedAttributeAnonymousClassList.get(i);
//                UMLAnonymousClassDiff anonymousClassDiff = new UMLAnonymousClassDiff(anonymousClass1, anonymousClass2, classDiff, modelDiff);
//                anonymousClassDiff.process();
//                anonymousClassDiffList.add(anonymousClassDiff);
//            }
//        }
//        this.addedGetter = findMethod(classDiff.getAddedOperations(), addedAttribute, getterCondition(addedAttribute));
//        if(this.addedGetter != null && !removedAttribute.getName().equals(addedAttribute.getName())) {
//            UMLOperation removedGetter = findMethod(classDiff.getRemovedOperations(), removedAttribute, getterCondition(removedAttribute));
//            if(removedGetter != null) {
//                this.addedGetter = null;
//            }
//        }
//        this.addedSetter = findMethod(classDiff.getAddedOperations(), addedAttribute, setterCondition(addedAttribute));
//        if(this.addedSetter != null && !removedAttribute.getName().equals(addedAttribute.getName())) {
//            UMLOperation removedSetter = findMethod(classDiff.getRemovedOperations(), removedAttribute, setterCondition(removedAttribute));
//            if(removedSetter != null) {
//                this.addedSetter = null;
//            }
//        }
//    }
//
//    private Function<UMLOperation, Boolean> getterCondition(UMLAttribute attribute) {
//        return (UMLOperation operation) -> operation.isGetter() && (operation.getReturnParameter().getType().equals(attribute.getType()) ||
//                operation.getReturnParameter().getType().getClassType().equalsIgnoreCase(attribute.getType().getClassType()));
//    }
//
//    private Function<UMLOperation, Boolean> setterCondition(UMLAttribute attribute) {
//        return (UMLOperation operation) -> operation.isSetter() && (operation.getParameterTypeList().get(0).equals(attribute.getType()) ||
//                operation.getParameterTypeList().get(0).getClassType().equalsIgnoreCase(attribute.getType().getClassType()));
//    }
//
//    private UMLOperation findMethod(List<UMLOperation> operations, UMLAttribute attribute, Function<UMLOperation, Boolean> condition) {
//        for(UMLOperation operation : operations) {
//            if(!operation.isConstructor() && !operation.hasOverrideAnnotation() && condition.apply(operation)) {
//                List<String> variables = operation.getAllVariables();
//                if(variables.contains(attribute.getName()) || variables.contains("this." + attribute.getName())) {
//                    return operation;
//                }
//            }
//        }
//        return null;
//    }
//
//    public UMLAttributeDiff(UMLAttribute removedAttribute, UMLAttribute addedAttribute, List<UMLOperationBodyMapper> operationBodyMapperList) {
//        this.removedAttribute = removedAttribute;
//        this.addedAttribute = addedAttribute;
//        this.operationBodyMapperList = operationBodyMapperList;
//        this.anonymousClassDiffList = new ArrayList<UMLAnonymousClassDiff>();
//        this.visibilityChanged = false;
//        this.typeChanged = false;
//        this.renamed = false;
//        this.staticChanged = false;
//        this.finalChanged = false;
//        if(!removedAttribute.getName().equals(addedAttribute.getName()))
//            renamed = true;
//        if(!removedAttribute.getVisibility().equals(addedAttribute.getVisibility()))
//            visibilityChanged = true;
//        if(!removedAttribute.getType().equals(addedAttribute.getType()))
//            typeChanged = true;
//        else if(!removedAttribute.getType().equalsQualified(addedAttribute.getType()))
//            qualifiedTypeChanged = true;
//        if(removedAttribute.isStatic() != addedAttribute.isStatic())
//            staticChanged = true;
//        if(removedAttribute.isFinal() != addedAttribute.isFinal())
//            finalChanged = true;
//        if(removedAttribute.isVolatile() != addedAttribute.isVolatile())
//            volatileChanged = true;
//        if(removedAttribute.isTransient() != addedAttribute.isTransient())
//            transientChanged = true;
//        this.annotationListDiff = new UMLAnnotationListDiff(removedAttribute.getAnnotations(), addedAttribute.getAnnotations());
//    }

    public UMLAttribute getRemovedAttribute() {
        return removedAttribute;
    }

    public UMLAttribute getAddedAttribute() {
        return addedAttribute;
    }

    public boolean isRenamed() {
        return renamed;
    }

    public boolean isVisibilityChanged() {
        return visibilityChanged;
    }

    public boolean isTypeChanged() {
        return typeChanged;
    }

    public boolean isQualifiedTypeChanged() {
        return qualifiedTypeChanged;
    }

    public boolean isEmpty() {
        return !visibilityChanged && !staticChanged && !finalChanged
                && !volatileChanged && !transientChanged && !typeChanged && !renamed
                && !qualifiedTypeChanged /*&& annotationListDiff.isEmpty()
                && anonymousClassDiffList.isEmpty()*/ && addedGetter == null && addedSetter == null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!isEmpty())
            sb.append("\t").append(removedAttribute).append("\n");
        if (renamed)
            sb.append("\t").append("renamed from " + removedAttribute.getName() + " to " + addedAttribute.getName()).append("\n");
        if (visibilityChanged)
            sb.append("\t").append("visibility changed from " + removedAttribute.getVisibility() + " to " + addedAttribute.getVisibility()).append("\n");
//        if(typeChanged || qualifiedTypeChanged)
//            sb.append("\t").append("type changed from " + removedAttribute.getType() + " to " + addedAttribute.getType()).append("\n");
        if (staticChanged)
            sb.append("\t").append("modifier changed from " + (removedAttribute.isStatic() ? "static" : "non-static") + " to " +
                    (addedAttribute.isStatic() ? "static" : "non-static")).append("\n");
        if (finalChanged)
            sb.append("\t").append("modifier changed from " + (removedAttribute.isFinal() ? "final" : "non-final") + " to " +
                    (addedAttribute.isFinal() ? "final" : "non-final")).append("\n");
        return sb.toString();
    }

//    private Set<Refactoring> getAnnotationRefactorings() {
//        Set<Refactoring> refactorings = new LinkedHashSet<>();
//        for(UMLAnnotation annotation : annotationListDiff.getAddedAnnotations()) {
//            AddAttributeAnnotationRefactoring refactoring = new AddAttributeAnnotationRefactoring(annotation, removedAttribute, addedAttribute);
//            refactorings.add(refactoring);
//        }
//        for(UMLAnnotation annotation : annotationListDiff.getRemovedAnnotations()) {
//            RemoveAttributeAnnotationRefactoring refactoring = new RemoveAttributeAnnotationRefactoring(annotation, removedAttribute, addedAttribute);
//            refactorings.add(refactoring);
//        }
//        for(UMLAnnotationDiff annotationDiff : annotationListDiff.getAnnotationDiffList()) {
//            ModifyAttributeAnnotationRefactoring refactoring = new ModifyAttributeAnnotationRefactoring(annotationDiff.getRemovedAnnotation(), annotationDiff.getAddedAnnotation(), removedAttribute, addedAttribute);
//            refactorings.add(refactoring);
//        }
//        return refactorings;
//    }

//    private Set<Refactoring> getAnonymousClassRefactorings() {
//        Set<Refactoring> refactorings = new LinkedHashSet<Refactoring>();
//        for(var anonymousClassDiff : anonymousClassDiffList) {
//            refactorings.addAll(anonymousClassDiff.getRefactorings());
//        }
//        return refactorings;
//    }

    public Set<Refactoring> getRefactorings() {
        Set<Refactoring> refactorings = new LinkedHashSet<>();
//        if(changeTypeCondition()) {
//            ChangeAttributeTypeRefactoring ref = new ChangeAttributeTypeRefactoring(removedAttribute, addedAttribute,
//                    VariableReferenceExtractor.findReferences(removedAttribute.getVariableDeclaration(), addedAttribute.getVariableDeclaration(), operationBodyMapperList));
//            refactorings.add(ref);
//        }
//        refactorings.addAll(getModifierRefactorings());
//        refactorings.addAll(getAnnotationRefactorings());
//        refactorings.addAll(getAnonymousClassRefactorings());
        return refactorings;
    }
//
//    private Set<Refactoring> getModifierRefactorings() {
//        Set<Refactoring> refactorings = new LinkedHashSet<Refactoring>();
//        if(isVisibilityChanged()) {
//            ChangeAttributeAccessModifierRefactoring ref = new ChangeAttributeAccessModifierRefactoring(removedAttribute.getVisibility(), addedAttribute.getVisibility(), removedAttribute, addedAttribute);
//            refactorings.add(ref);
//        }
//        if(encapsulationCondition()) {
//            EncapsulateAttributeRefactoring ref = new EncapsulateAttributeRefactoring(removedAttribute, addedAttribute, addedGetter, addedSetter);
//            refactorings.add(ref);
//        }
//        if(staticChanged) {
//            if(addedAttribute.isStatic()) {
//                AddAttributeModifierRefactoring ref = new AddAttributeModifierRefactoring("static", removedAttribute, addedAttribute);
//                refactorings.add(ref);
//            }
//            else if(removedAttribute.isStatic()) {
//                RemoveAttributeModifierRefactoring ref = new RemoveAttributeModifierRefactoring("static", removedAttribute, addedAttribute);
//                refactorings.add(ref);
//            }
//        }
//
//        return refactorings;
//    }

    public Set<Refactoring> getRefactorings(Set<CandidateAttributeRefactoring> set) {
        Set<Refactoring> refactorings = new LinkedHashSet<>();
//        RenameAttributeRefactoring rename = null;
//        if(isRenamed()) {
//            rename = new RenameAttributeRefactoring(removedAttribute, addedAttribute, set);
//            refactorings.add(rename);
//        }
//        if(changeTypeCondition()) {
//            ChangeAttributeTypeRefactoring ref = new ChangeAttributeTypeRefactoring(removedAttribute, addedAttribute,
//                    VariableReferenceExtractor.findReferences(removedAttribute.getVariableDeclaration(), addedAttribute.getVariableDeclaration(), operationBodyMapperList));
//            refactorings.add(ref);
//            if(rename != null) {
//                ref.addRelatedRefactoring(rename);
//            }
//        }
        // refactorings.addAll(getModifierRefactorings());
        //refactorings.addAll(getAnnotationRefactorings());
        //refactorings.addAll(getAnonymousClassRefactorings());
        return refactorings;
    }

    private boolean encapsulationCondition() {
        return addedSetter != null || addedGetter != null;
    }

    private boolean changeTypeCondition() {
        return (isTypeChanged() || isQualifiedTypeChanged()) && !enumConstantsDeclaredInTheSameEnumDeclarationType();
    }

    private boolean enumConstantsDeclaredInTheSameEnumDeclarationType() {
        VariableDeclaration removedVariableDeclaration = removedAttribute.getVariableDeclaration();
        VariableDeclaration addedVariableDeclaration = addedAttribute.getVariableDeclaration();
//        if(removedVariableDeclaration.isEnumConstant() && addedVariableDeclaration.isEnumConstant() &&
//                removedVariableDeclaration.getType().equals(addedVariableDeclaration.getType())) {
//            return true;
//        }
        return false;
    }
}
