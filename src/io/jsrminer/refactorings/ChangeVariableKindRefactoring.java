package io.jsrminer.refactorings;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.mapping.CodeFragmentMapping;

import java.util.LinkedHashSet;
import java.util.Set;

public class ChangeVariableKindRefactoring extends implements IRefactoring {
    private VariableDeclaration originalVariable;
    private VariableDeclaration changedTypeVariable;
    private FunctionDeclaration operationBefore;
    private FunctionDeclaration operationAfter;
    private Set<CodeFragmentMapping> variableReferences;
    private Set<Refactoring> relatedRefactorings;

    public ChangeVariableKindRefactoring(VariableDeclaration originalVariable, VariableDeclaration changedTypeVariable,
                                         FunctionDeclaration operationBefore, FunctionDeclaration operationAfter, Set<CodeFragmentMapping> variableReferences) {
        this.originalVariable = originalVariable;
        this.changedTypeVariable = changedTypeVariable;
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
        this.variableReferences = variableReferences;
        this.relatedRefactorings = new LinkedHashSet<Refactoring>();
    }

    public void addRelatedRefactoring(Refactoring refactoring) {
        this.relatedRefactorings.add(refactoring);
    }

    public Set<Refactoring> getRelatedRefactorings() {
        return relatedRefactorings;
    }

    public RefactoringType getRefactoringType() {
        if (originalVariable.isParameter() && changedTypeVariable.isParameter())
            return RefactoringType.CHANGE_PARAMETER_TYPE;
        return RefactoringType.CHANGE_VARIABLE_TYPE;
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public VariableDeclaration getOriginalVariable() {
        return originalVariable;
    }

    public VariableDeclaration getChangedTypeVariable() {
        return changedTypeVariable;
    }

    public FunctionDeclaration getOperationBefore() {
        return operationBefore;
    }

    public FunctionDeclaration getOperationAfter() {
        return operationAfter;
    }

    public Set<CodeFragmentMapping> getVariableReferences() {
        return variableReferences;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        //boolean qualified = originalVariable.getKind().equals(changedTypeVariable.getKind()) && !originalVariable.getType().equalsQualified(changedTypeVariable.getType());
        sb.append(getName()).append("\t");
        //b.append(qualified ? originalVariable.toQualifiedString() : originalVariable.toString());
        sb.append(originalVariable.toString());
        sb.append(" to ");
        //sb.append(qualified ? changedTypeVariable.toQualifiedString() : changedTypeVariable.toString());
        sb.append(changedTypeVariable.toString());
        sb.append(" in method ");
        //
        sb.append(operationAfter.toString());
        sb.append(" at ").append(operationAfter.getQualifiedName());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((changedTypeVariable == null) ? 0 : changedTypeVariable.hashCode());
        result = prime * result + ((operationAfter == null) ? 0 : operationAfter.hashCode());
        result = prime * result + ((operationBefore == null) ? 0 : operationBefore.hashCode());
        result = prime * result + ((originalVariable == null) ? 0 : originalVariable.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChangeVariableKindRefactoring other = (ChangeVariableKindRefactoring) obj;
        if (changedTypeVariable == null) {
            if (other.changedTypeVariable != null)
                return false;
        } else if (!changedTypeVariable.equals(other.changedTypeVariable))
            return false;
        if (operationAfter == null) {
            if (other.operationAfter != null)
                return false;
        } else if (!operationAfter.equals(other.operationAfter))
            return false;
        if (operationBefore == null) {
            if (other.operationBefore != null)
                return false;
        } else if (!operationBefore.equals(other.operationBefore))
            return false;
        if (originalVariable == null) {
            if (other.originalVariable != null)
                return false;
        } else if (!originalVariable.equals(other.originalVariable))
            return false;
        return true;
    }

//    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOperationBefore().getLocationInfo().getFilePath(), getOperationBefore().getClassName()));
//        return pairs;
//    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOperationAfter().getLocationInfo().getFilePath(), getOperationAfter().getClassName()));
//        return pairs;
//    }

//    @Override
//    public List<CodeRange> leftSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(originalVariable.codeRange()
//                .setDescription("original variable declaration")
//                .setCodeElement(originalVariable.toString()));
//        return ranges;
//    }
//
//    @Override
//    public List<CodeRange> rightSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(changedTypeVariable.codeRange()
//                .setDescription("changed-type variable declaration")
//                .setCodeElement(changedTypeVariable.toString()));
//        return ranges;
//    }
}
