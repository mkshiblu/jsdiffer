package io.jsrminer.refactorings;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.mapping.CodeFragmentMapping;

import java.util.Set;

public class MergeVariableRefactoring extends Refactoring implements IRefactoring {
    private Set<VariableDeclaration> mergedVariables;
    private VariableDeclaration newVariable;
    private FunctionDeclaration operationBefore;
    private FunctionDeclaration operationAfter;
    private Set<CodeFragmentMapping> variableReferences;

    public MergeVariableRefactoring(Set<VariableDeclaration> mergedVariables, VariableDeclaration newVariable,
                                    FunctionDeclaration operationBefore, FunctionDeclaration operationAfter, Set<CodeFragmentMapping> variableReferences) {
        this.mergedVariables = mergedVariables;
        this.newVariable = newVariable;
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
        this.variableReferences = variableReferences;
    }

    public Set<VariableDeclaration> getMergedVariables() {
        return mergedVariables;
    }

    public VariableDeclaration getNewVariable() {
        return newVariable;
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

    private boolean allVariablesAreParameters() {
        for (VariableDeclaration declaration : mergedVariables) {
            if (!declaration.isParameter()) {
                return false;
            }
        }
        return newVariable.isParameter();
    }

    public RefactoringType getRefactoringType() {
        if (allVariablesAreParameters())
            return RefactoringType.MERGE_PARAMETER;
        return RefactoringType.MERGE_VARIABLE;
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }
//
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(mergedVariables);
        sb.append(" to ");
        sb.append(newVariable);
        sb.append(" in method ");
        sb.append(operationAfter);
        sb.append(" at ").append(operationAfter.getFullyQualifiedName());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mergedVariables == null) ? 0 : mergedVariables.hashCode());
        result = prime * result + ((newVariable == null) ? 0 : newVariable.hashCode());
        result = prime * result + ((operationAfter == null) ? 0 : operationAfter.hashCode());
        result = prime * result + ((operationBefore == null) ? 0 : operationBefore.hashCode());
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
        MergeVariableRefactoring other = (MergeVariableRefactoring) obj;
        if (mergedVariables == null) {
            if (other.mergedVariables != null)
                return false;
        } else if (!mergedVariables.equals(other.mergedVariables))
            return false;
        if (newVariable == null) {
            if (other.newVariable != null)
                return false;
        } else if (!newVariable.equals(other.newVariable))
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
        return true;
    }

//    @Override
//    public List<CodeRange> leftSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        for(VariableDeclaration mergedVariable : mergedVariables) {
//            ranges.add(mergedVariable.codeRange()
//                    .setDescription("merged variable declaration")
//                    .setCodeElement(mergedVariable.toString()));
//        }
//        return ranges;
//    }
//
//    @Override
//    public List<CodeRange> rightSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(newVariable.codeRange()
//                .setDescription("new variable declaration")
//                .setCodeElement(newVariable.toString()));
//        return ranges;
//    }
}