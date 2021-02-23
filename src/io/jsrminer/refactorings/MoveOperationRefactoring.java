package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.Replacement;

import java.util.LinkedHashSet;
import java.util.Set;

public class MoveOperationRefactoring extends Refactoring {
    protected FunctionDeclaration originalOperation;
    protected FunctionDeclaration movedOperation;
    private Set<Replacement> replacements;
    private FunctionBodyMapper bodyMapper;

    public MoveOperationRefactoring(FunctionBodyMapper bodyMapper) {
        this.bodyMapper = bodyMapper;
        this.originalOperation = bodyMapper.getOperation1();
        this.movedOperation = bodyMapper.getOperation2();
        this.replacements = bodyMapper.getReplacements();
    }

    public MoveOperationRefactoring(FunctionDeclaration originalOperation, FunctionDeclaration movedOperation) {
        this.originalOperation = originalOperation;
        this.movedOperation = movedOperation;
        this.replacements = new LinkedHashSet<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("    ");
        sb.append(originalOperation);
        sb.append(" from ");
        sb.append(originalOperation.getParentContainerQualifiedName());
        sb.append(" to ");
        sb.append(movedOperation);
        sb.append(" at ");
        sb.append(movedOperation.getParentContainerQualifiedName());
        return sb.toString();
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public RefactoringType getRefactoringType() {
        if(!originalOperation.getName().equals(movedOperation.getName())) {
            return RefactoringType.MOVE_AND_RENAME_OPERATION;
        }
        return RefactoringType.MOVE_OPERATION;
    }

    public FunctionBodyMapper getBodyMapper() {
        return bodyMapper;
    }

    public FunctionDeclaration getOriginalOperation() {
        return originalOperation;
    }

    public FunctionDeclaration getMovedOperation() {
        return movedOperation;
    }

    public Set<Replacement> getReplacements() {
        return replacements;
    }

//    /**
//     * @return the code range of the source method in the <b>parent</b> commit
//     */
//    public CodeRange getSourceOperationCodeRangeBeforeMove() {
//        return originalOperation.codeRange();
//    }
//
//    /**
//     * @return the code range of the target method in the <b>child</b> commit
//     */
//    public CodeRange getTargetOperationCodeRangeAfterMove() {
//        return movedOperation.codeRange();
//    }

//    public boolean compatibleWith(MoveAttributeRefactoring ref) {
//        if(ref.getMovedAttribute().getClassName().equals(this.movedOperation.getClassName()) &&
//                ref.getOriginalAttribute().getClassName().equals(this.originalOperation.getClassName())) {
//            List<String> originalOperationVariables = this.originalOperation.getAllVariables();
//            List<String> movedOperationVariables = this.movedOperation.getAllVariables();
//            return originalOperationVariables.contains(ref.getOriginalAttribute().getName()) &&
//                    movedOperationVariables.contains(ref.getMovedAttribute().getName());
//        }
//        return false;
//    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOriginalOperation().getLocationInfo().getFilePath(), getOriginalOperation().getClassName()));
//        return pairs;
//    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getMovedOperation().getLocationInfo().getFilePath(), getMovedOperation().getClassName()));
//        return pairs;
//    }

//    @Override
//    public List<CodeRange> leftSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(originalOperation.codeRange()
//                .setDescription("original method declaration")
//                .setCodeElement(originalOperation.toString()));
//        return ranges;
//    }
//
//    @Override
//    public List<CodeRange> rightSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(movedOperation.codeRange()
//                .setDescription("moved method declaration")
//                .setCodeElement(movedOperation.toString()));
//        return ranges;
//    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((movedOperation == null) ? 0 : movedOperation.hashCode());
        result = prime * result + ((movedOperation == null) ? 0 : movedOperation.getSourceLocation().hashCode());
        result = prime * result + ((originalOperation == null) ? 0 : originalOperation.hashCode());
        result = prime * result + ((originalOperation == null) ? 0 : originalOperation.getSourceLocation().hashCode());
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
        MoveOperationRefactoring other = (MoveOperationRefactoring) obj;
        if (movedOperation == null) {
            if (other.movedOperation != null)
                return false;
        } else if (!movedOperation.equals(other.movedOperation)) {
            return false;
        } else if(!movedOperation.getSourceLocation().equals(other.movedOperation.getSourceLocation())) {
            return false;
        }
        if (originalOperation == null) {
            if (other.originalOperation != null)
                return false;
        } else if (!originalOperation.equals(other.originalOperation)) {
            return false;
        } else if (!originalOperation.getSourceLocation().equals(other.originalOperation.getSourceLocation())) {
            return false;
        }
        return true;
    }
}
