package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.UMLAttribute;
import io.jsrminer.uml.mapping.CodeFragmentMapping;

import java.util.Set;

public class CandidateAttributeRefactoring {
    private String originalVariableName;
    private String renamedVariableName;
    private FunctionDeclaration operationBefore;
    private FunctionDeclaration operationAfter;
    private Set<CodeFragmentMapping> attributeReferences;
    private VariableDeclaration originalVariableDeclaration;
    private VariableDeclaration renamedVariableDeclaration;
    private UMLAttribute originalAttribute;
    private UMLAttribute renamedAttribute;

    public CandidateAttributeRefactoring(
            String originalVariableName,
            String renamedVariableName,
            FunctionDeclaration operationBefore,
            FunctionDeclaration operationAfter,
            Set<CodeFragmentMapping> attributeReferences) {
        this.originalVariableName = originalVariableName;
        this.renamedVariableName = renamedVariableName;
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
        this.attributeReferences = attributeReferences;
    }

    public String getOriginalVariableName() {
        return originalVariableName;
    }

    public String getRenamedVariableName() {
        return renamedVariableName;
    }

    public FunctionDeclaration getOperationBefore() {
        return operationBefore;
    }

    public FunctionDeclaration getOperationAfter() {
        return operationAfter;
    }

    public Set<CodeFragmentMapping> getAttributeReferences() {
        return attributeReferences;
    }

    public int getOccurrences() {
        return attributeReferences.size();
    }

    public VariableDeclaration getOriginalVariableDeclaration() {
        return originalVariableDeclaration;
    }

    public void setOriginalVariableDeclaration(VariableDeclaration originalVariableDeclaration) {
        this.originalVariableDeclaration = originalVariableDeclaration;
    }

    public VariableDeclaration getRenamedVariableDeclaration() {
        return renamedVariableDeclaration;
    }

    public void setRenamedVariableDeclaration(VariableDeclaration renamedVariableDeclaration) {
        this.renamedVariableDeclaration = renamedVariableDeclaration;
    }

    public UMLAttribute getOriginalAttribute() {
        return originalAttribute;
    }

    public void setOriginalAttribute(UMLAttribute originalAttribute) {
        this.originalAttribute = originalAttribute;
    }

    public UMLAttribute getRenamedAttribute() {
        return renamedAttribute;
    }

    public void setRenamedAttribute(UMLAttribute renamedAttribute) {
        this.renamedAttribute = renamedAttribute;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rename Attribute").append("\t");
        sb.append(originalVariableName);
        sb.append(" to ");
        sb.append(renamedVariableName);
        sb.append(" in method ");
        sb.append(operationAfter);
        sb.append(" at ").append(operationAfter.getFullyQualifiedName());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operationAfter == null) ? 0 : operationAfter.hashCode());
        result = prime * result + ((operationBefore == null) ? 0 : operationBefore.hashCode());
        result = prime * result + ((originalVariableName == null) ? 0 : originalVariableName.hashCode());
        result = prime * result + ((renamedVariableName == null) ? 0 : renamedVariableName.hashCode());
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
        CandidateAttributeRefactoring other = (CandidateAttributeRefactoring) obj;
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
        if (originalVariableName == null) {
            if (other.originalVariableName != null)
                return false;
        } else if (!originalVariableName.equals(other.originalVariableName))
            return false;
        if (renamedVariableName == null) {
            if (other.renamedVariableName != null)
                return false;
        } else if (!renamedVariableName.equals(other.renamedVariableName))
            return false;
        return true;
    }
}
