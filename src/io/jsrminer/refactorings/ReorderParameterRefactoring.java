package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.UMLParameter;

import java.util.ArrayList;
import java.util.List;

public class ReorderParameterRefactoring extends Refactoring {
    private List<VariableDeclaration> parametersBefore;
    private List<VariableDeclaration> parametersAfter;
    private FunctionDeclaration operationBefore;
    private FunctionDeclaration operationAfter;

    public ReorderParameterRefactoring(FunctionDeclaration operationBefore, FunctionDeclaration operationAfter) {
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
        this.parametersBefore = new ArrayList<VariableDeclaration>();
        for (UMLParameter parameter : operationBefore.getParameters()) {
            parametersBefore.add(parameter.getVariableDeclaration());
        }
        this.parametersAfter = new ArrayList<VariableDeclaration>();
        for (UMLParameter parameter : operationAfter.getParameters()) {
            parametersAfter.add(parameter.getVariableDeclaration());
        }
    }

    public List<VariableDeclaration> getParametersBefore() {
        return parametersBefore;
    }

    public List<VariableDeclaration> getParametersAfter() {
        return parametersAfter;
    }

    public FunctionDeclaration getOperationBefore() {
        return operationBefore;
    }

    public FunctionDeclaration getOperationAfter() {
        return operationAfter;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return RefactoringType.REORDER_PARAMETER;
    }

    @Override
    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(parametersBefore);
        sb.append(" to ");
        sb.append(parametersAfter);
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
        result = prime * result + ((parametersAfter == null) ? 0 : parametersAfter.hashCode());
        result = prime * result + ((parametersBefore == null) ? 0 : parametersBefore.hashCode());
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
        ReorderParameterRefactoring other = (ReorderParameterRefactoring) obj;
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
        if (parametersAfter == null) {
            if (other.parametersAfter != null)
                return false;
        } else if (!parametersAfter.equals(other.parametersAfter))
            return false;
        if (parametersBefore == null) {
            if (other.parametersBefore != null)
                return false;
        } else if (!parametersBefore.equals(other.parametersBefore))
            return false;
        return true;
    }
}