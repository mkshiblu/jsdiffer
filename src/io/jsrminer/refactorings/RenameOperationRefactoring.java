package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.Replacement;

import java.util.LinkedHashSet;
import java.util.Set;

public class RenameOperationRefactoring extends Refactoring {
    private final FunctionDeclaration originalOperation;
    private final FunctionDeclaration renamedOperation;
    private Set<Replacement> replacements = new LinkedHashSet<>();
    private FunctionBodyMapper bodyMapper;

    public RenameOperationRefactoring(FunctionBodyMapper bodyMapper) {
        this(bodyMapper.function1, bodyMapper.function2);
        this.bodyMapper = bodyMapper;
        this.replacements = bodyMapper.getReplacements();
    }

    public RenameOperationRefactoring(FunctionDeclaration originalOperation, FunctionDeclaration renamedOperation) {
        super((RefactoringType.RENAME_METHOD));
        this.originalOperation = originalOperation;
        this.renamedOperation = renamedOperation;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(originalOperation);
        sb.append(" renamed to ");
        sb.append(renamedOperation);
        sb.append(" in class ").append(getClassName());
        return sb.toString();
    }

    private String getClassName() {
//        String sourceClassName = originalOperation.getClassName();
//        String targetClassName = renamedOperation.getClassName();
//        boolean targetIsAnonymousInsideSource = false;
//        if (targetClassName.startsWith(sourceClassName + ".")) {
//            String targetClassNameSuffix = targetClassName.substring(sourceClassName.length() + 1, targetClassName.length());
//            targetIsAnonymousInsideSource = isNumeric(targetClassNameSuffix);
//        }
//        return sourceClassName.equals(targetClassName) || targetIsAnonymousInsideSource ? sourceClassName : targetClassName;
        return "%%%";
    }

    private static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public FunctionBodyMapper getBodyMapper() {
        return bodyMapper;
    }

    public FunctionDeclaration getOriginalOperation() {
        return originalOperation;
    }

    public FunctionDeclaration getRenamedOperation() {
        return renamedOperation;
    }

    public Set<Replacement> getReplacements() {
        return replacements;
    }
//
//    /**
//     * @return the code range of the source method in the <b>parent</b> commit
//     */
//    public CodeRange getSourceOperationCodeRangeBeforeRename() {
//        return originalOperation.codeRange();
//    }
//
//    /**
//     * @return the code range of the target method in the <b>child</b> commit
//     */
//    public CodeRange getTargetOperationCodeRangeAfterRename() {
//        return renamedOperation.codeRange();
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
//        pairs.add(new ImmutablePair<String, String>(getRenamedOperation().getLocationInfo().getFilePath(), getRenamedOperation().getClassName()));
//        return pairs;
//    }
//
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
//        ranges.add(renamedOperation.codeRange()
//                .setDescription("renamed method declaration")
//                .setCodeElement(renamedOperation.toString()));
//        return ranges;
//    }

//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((renamedOperation == null) ? 0 : renamedOperation.hashCode());
//        result = prime * result + ((renamedOperation == null) ? 0 : renamedOperation.getLocationInfo().hashCode());
//        result = prime * result + ((originalOperation == null) ? 0 : originalOperation.hashCode());
//        result = prime * result + ((originalOperation == null) ? 0 : originalOperation.getLocationInfo().hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        RenameOperationRefactoring other = (RenameOperationRefactoring) obj;
//        if (renamedOperation == null) {
//            if (other.renamedOperation != null)
//                return false;
//        } else if (!renamedOperation.equals(other.renamedOperation)) {
//            return false;
//        } else if (!renamedOperation.getLocationInfo().equals(other.renamedOperation.getLocationInfo())) {
//            return false;
//        }
//        if (originalOperation == null) {
//            if (other.originalOperation != null)
//                return false;
//        } else if (!originalOperation.equals(other.originalOperation)) {
//            return false;
//        } else if (!originalOperation.getLocationInfo().equals(other.originalOperation.getLocationInfo())) {
//            return false;
//        }
//        return true;
//    }
}

