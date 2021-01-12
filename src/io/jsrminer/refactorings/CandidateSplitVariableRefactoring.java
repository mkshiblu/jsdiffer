package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.mapping.CodeFragmentMapping;

import java.util.Set;

public class CandidateSplitVariableRefactoring {
    private String oldVariable;
    private Set<String> splitVariables;
    private FunctionDeclaration operationBefore;
    private FunctionDeclaration operationAfter;
    private Set<CodeFragmentMapping> variableReferences;
    //private UMLAttribute oldAttribute;
    //private Set<UMLAttribute> splitAttributes;

    public CandidateSplitVariableRefactoring(String oldVariable, Set<String> splitVariables,
                                             FunctionDeclaration operationBefore, FunctionDeclaration operationAfter, Set<CodeFragmentMapping> variableReferences) {
        this.oldVariable = oldVariable;
        this.splitVariables = splitVariables;
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
        this.variableReferences = variableReferences;
    }

    public String getOldVariable() {
        return oldVariable;
    }

    public Set<String> getSplitVariables() {
        return splitVariables;
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

//    public Set<UMLAttribute> getSplitAttributes() {
//        return splitAttributes;
//    }
//
//    public void setSplitAttributes(Set<UMLAttribute> splitAttributes) {
//        this.splitAttributes = splitAttributes;
//    }

//    public UMLAttribute getOldAttribute() {
//        return oldAttribute;
//    }

//    public void setOldAttribute(UMLAttribute oldAttribute) {
//        this.oldAttribute = oldAttribute;
//    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Split Attribute").append("\t");
        sb.append(oldVariable);
        sb.append(" to ");
        sb.append(splitVariables);
        sb.append(" in method ");
        sb.append(operationAfter);
        sb.append(" at ").append(operationAfter.getFullyQualifiedName());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((oldVariable == null) ? 0 : oldVariable.hashCode());
        result = prime * result + ((operationAfter == null) ? 0 : operationAfter.hashCode());
        result = prime * result + ((operationBefore == null) ? 0 : operationBefore.hashCode());
        result = prime * result + ((splitVariables == null) ? 0 : splitVariables.hashCode());
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
        CandidateSplitVariableRefactoring other = (CandidateSplitVariableRefactoring) obj;
        if (oldVariable == null) {
            if (other.oldVariable != null)
                return false;
        } else if (!oldVariable.equals(other.oldVariable))
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
        if (splitVariables == null) {
            if (other.splitVariables != null)
                return false;
        } else if (!splitVariables.equals(other.splitVariables))
            return false;
        return true;
    }

}