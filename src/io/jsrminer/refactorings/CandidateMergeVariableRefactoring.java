package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.mapping.CodeFragmentMapping;

import java.util.Set;

public class CandidateMergeVariableRefactoring {
    private Set<String> mergedVariables;
    private String newVariable;
    private FunctionDeclaration operationBefore;
    private FunctionDeclaration operationAfter;
    private Set<CodeFragmentMapping> variableReferences;
    //private Set<UMLAttribute> mergedAttributes;
    //private UMLAttribute newAttribute;

    public CandidateMergeVariableRefactoring(Set<String> mergedVariables, String newVariable,
                                             FunctionDeclaration operationBefore, FunctionDeclaration operationAfter, Set<CodeFragmentMapping> variableReferences) {
        this.mergedVariables = mergedVariables;
        this.newVariable = newVariable;
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
        this.variableReferences = variableReferences;
    }

    public Set<String> getMergedVariables() {
        return mergedVariables;
    }

    public String getNewVariable() {
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

//    public Set<UMLAttribute> getMergedAttributes() {
//        return mergedAttributes;
//    }

    //    public void setMergedAttributes(Set<UMLAttribute> mergedAttributes) {
//        this.mergedAttributes = mergedAttributes;
//    }
//
//    public UMLAttribute getNewAttribute() {
//        return newAttribute;
//    }
//
//    public void setNewAttribute(UMLAttribute newAttribute) {
//        this.newAttribute = newAttribute;
//    }
//
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Merge Attribute").append("\t");
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
        CandidateMergeVariableRefactoring other = (CandidateMergeVariableRefactoring) obj;
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
}
