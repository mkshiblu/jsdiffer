package io.jsrminer.refactorings;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.rminerx.core.entities.Container;

import java.util.Set;

public class RenameVariableRefactoring extends Refactoring implements IRefactoring {

    private VariableDeclaration originalVariable;
    private VariableDeclaration renamedVariable;
    private Container operationBefore;
    private Container operationAfter;
    private Set<CodeFragmentMapping> variableReferences;

    public RenameVariableRefactoring(
            VariableDeclaration originalVariable,
            VariableDeclaration renamedVariable,
            Container operationBefore,
            Container operationAfter,
            Set<CodeFragmentMapping> variableReferences) {
        this.originalVariable = originalVariable;
        this.renamedVariable = renamedVariable;
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
        this.variableReferences = variableReferences;
    }

    public RefactoringType getRefactoringType() {
//        if (originalVariable.isParameter() && renamedVariable.isParameter())
//            return RefactoringType.RENAME_PARAMETER;
//        if (!originalVariable.isParameter() && renamedVariable.isParameter())
//            return RefactoringType.PARAMETERIZE_VARIABLE;
//        if (!originalVariable.isAttribute() && renamedVariable.isAttribute())
//            return RefactoringType.REPLACE_VARIABLE_WITH_ATTRIBUTE;
        return RefactoringType.RENAME_VARIABLE;
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public VariableDeclaration getOriginalVariable() {
        return originalVariable;
    }

    public VariableDeclaration getRenamedVariable() {
        return renamedVariable;
    }

    public Container getOperationBefore() {
        return operationBefore;
    }

    public Container getOperationAfter() {
        return operationAfter;
    }

    public Set<CodeFragmentMapping> getVariableReferences() {
        return variableReferences;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(originalVariable);
        sb.append(" to ");
        sb.append(renamedVariable);
        sb.append(" in ");
        sb.append(operationAfter);
        sb.append(" at ");
        sb.append(operationAfter.getQualifiedName());
//        sb.append(" in class ").append(operationAfter.getClassName());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operationAfter == null) ? 0 : operationAfter.hashCode());
        result = prime * result + ((operationBefore == null) ? 0 : operationBefore.hashCode());
        result = prime * result + ((originalVariable == null) ? 0 : originalVariable.hashCode());
        result = prime * result + ((renamedVariable == null) ? 0 : renamedVariable.hashCode());
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
        RenameVariableRefactoring other = (RenameVariableRefactoring) obj;
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
        if (renamedVariable == null) {
            if (other.renamedVariable != null)
                return false;
        } else if (!renamedVariable.equals(other.renamedVariable))
            return false;
        return true;
    }

//    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOperationBefore().getSourceLocation().getFile(), getOperationBefore().getQualifiedName()));
//        return pairs;
//    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<>();
//        pairs.add(new ImmutablePair<String, String>(getOperationAfter().getSourceLocation().getFile(), getOperationAfter().getQualifiedName()));
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
//        ranges.add(renamedVariable.codeRange()
//                .setDescription("renamed variable declaration")
//                .setCodeElement(renamedVariable.toString()));
//        return ranges;
//    }
}

