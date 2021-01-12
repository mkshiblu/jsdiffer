package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.UMLParameter;

public class RemoveParameterRefactoring extends Refactoring {
    private UMLParameter parameter;
    private FunctionDeclaration operationBefore;
    private FunctionDeclaration operationAfter;

    public RemoveParameterRefactoring(UMLParameter parameter, FunctionDeclaration operationBefore,
                                      FunctionDeclaration operationAfter) {
        this.parameter = parameter;
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
    }

    public UMLParameter getParameter() {
        return parameter;
    }

    public FunctionDeclaration getOperationBefore() {
        return operationBefore;
    }

    public FunctionDeclaration getOperationAfter() {
        return operationAfter;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return RefactoringType.REMOVE_PARAMETER;
    }

    @Override
    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

//    @Override
//    public List<CodeRange> leftSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(parameter.getVariableDeclaration().codeRange()
//                .setDescription("removed parameter")
//                .setCodeElement(parameter.getVariableDeclaration().toString()));
//        ranges.add(operationBefore.codeRange()
//                .setDescription("original method declaration")
//                .setCodeElement(operationBefore.toString()));
//        return ranges;
//    }
//
//    @Override
//    public List<CodeRange> rightSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(operationAfter.codeRange()
//                .setDescription("method declaration with removed parameter")
//                .setCodeElement(operationAfter.toString()));
//        return ranges;
//    }


//    @Override
//    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOperationBefore().getLocationInfo().getFilePath(), getOperationBefore().getClassName()));
//        return pairs;
//    }
//
//    @Override
//    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOperationAfter().getLocationInfo().getFilePath(), getOperationAfter().getClassName()));
//        return pairs;
//    }
//
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(parameter.getVariableDeclaration());
        sb.append(" in method ");
        sb.append(operationBefore);
        sb.append(" from ");
        sb.append(operationBefore.getFullyQualifiedName());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameter.getVariableDeclaration() == null) ? 0 : parameter.getVariableDeclaration().hashCode());
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
        RemoveParameterRefactoring other = (RemoveParameterRefactoring) obj;
        if (parameter == null) {
            if (other.parameter != null)
                return false;
        } else if (!parameter.getVariableDeclaration().equals(other.parameter.getVariableDeclaration()))
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
