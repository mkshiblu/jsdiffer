package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.Replacement;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InlineOperationRefactoring extends Refactoring {
    private FunctionDeclaration inlinedOperation;
    private FunctionDeclaration targetOperationAfterInline;
    private FunctionDeclaration targetOperationBeforeInline;
    private List<OperationInvocation> inlinedOperationInvocations;
    private Set<Replacement> replacements;
    private Set<CodeFragment> inlinedCodeFragmentsFromInlinedOperation;
    private Set<CodeFragment> inlinedCodeFragmentsInTargetOperation;
    private FunctionBodyMapper bodyMapper;

    public InlineOperationRefactoring(FunctionBodyMapper bodyMapper, FunctionDeclaration targetOperationBeforeInline,
                                      List<OperationInvocation> operationInvocations) {
        this.bodyMapper = bodyMapper;
        this.inlinedOperation = bodyMapper.function1;
        this.targetOperationAfterInline = bodyMapper.function2;
        this.targetOperationBeforeInline = targetOperationBeforeInline;
        this.inlinedOperationInvocations = operationInvocations;
        this.replacements = bodyMapper.getReplacements();
        this.inlinedCodeFragmentsFromInlinedOperation = new LinkedHashSet<>();
        this.inlinedCodeFragmentsInTargetOperation = new LinkedHashSet<>();
        for (CodeFragmentMapping mapping : bodyMapper.getMappings()) {
            this.inlinedCodeFragmentsFromInlinedOperation.add(mapping.fragment1);
            this.inlinedCodeFragmentsInTargetOperation.add(mapping.fragment2);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(inlinedOperation);
        if (getRefactoringType().equals(RefactoringType.INLINE_OPERATION)) {
            sb.append(" inlined to ");
            sb.append(targetOperationAfterInline);
            sb.append(" at ");
            sb.append(targetOperationAfterInline.getFullyQualifiedName());
        } else if (getRefactoringType().equals(RefactoringType.MOVE_INLINE_OPERATION)) {
            sb.append(" moved from  ");
            sb.append(inlinedOperation.getFullyQualifiedName());
            sb.append(" to ");
            sb.append(getTargetOperationAfterInline().getFullyQualifiedName());
            sb.append(" & inlined to ");
            sb.append(getTargetOperationAfterInline());
        }
        return sb.toString();
    }
//
//    private String getClassName() {
//        return targetOperationAfterInline.getClassName();
//    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public RefactoringType getRefactoringType() {
        //if (!getTargetOperationBeforeInline().getClassName().equals(getInlinedOperation().getClassName()))
        //  return RefactoringType.MOVE_AND_INLINE_OPERATION;
        return RefactoringType.INLINE_OPERATION;
    }

    public FunctionBodyMapper getBodyMapper() {
        return bodyMapper;
    }

    public FunctionDeclaration getInlinedOperation() {
        return inlinedOperation;
    }

    public FunctionDeclaration getTargetOperationAfterInline() {
        return targetOperationAfterInline;
    }

    public FunctionDeclaration getTargetOperationBeforeInline() {
        return targetOperationBeforeInline;
    }

    public List<OperationInvocation> getInlinedOperationInvocations() {
        return inlinedOperationInvocations;
    }

    public Set<Replacement> getReplacements() {
        return replacements;
    }

    public Set<CodeFragment> getInlinedCodeFragments() {
        return inlinedCodeFragmentsInTargetOperation;
    }
//
//    /**
//     * @return the code range of the target method in the <b>parent</b> commit
//     */
//    public CodeRange getTargetOperationCodeRangeBeforeInline() {
//        return targetOperationBeforeInline.codeRange();
//    }
//
//    /**
//     * @return the code range of the target method in the <b>child</b> commit
//     */
//    public CodeRange getTargetOperationCodeRangeAfterInline() {
//        return targetOperationAfterInline.codeRange();
//    }
//
//    /**
//     * @return the code range of the inlined method in the <b>parent</b> commit
//     */
//    public CodeRange getInlinedOperationCodeRange() {
//        return inlinedOperation.codeRange();
//    }
//
//    /**
//     * @return the code range of the inlined code fragment from the inlined method in the <b>parent</b> commit
//     */
//    public CodeRange getInlinedCodeRangeFromInlinedOperation() {
//        return CodeRange.computeRange(inlinedCodeFragmentsFromInlinedOperation);
//    }
//
//    /**
//     * @return the code range of the inlined code fragment in the target method in the <b>child</b> commit
//     */
//    public CodeRange getInlinedCodeRangeInTargetOperation() {
//        return CodeRange.computeRange(inlinedCodeFragmentsInTargetOperation);
//    }
//
//    /**
//     * @return the code range(s) of the invocation(s) to the inlined method inside the target method in the <b>parent</b> commit
//     */
//    public Set<CodeRange> getInlinedOperationInvocationCodeRanges() {
//        Set<CodeRange> codeRanges = new LinkedHashSet<CodeRange>();
//        for (OperationInvocation invocation : inlinedOperationInvocations) {
//            codeRanges.add(invocation.codeRange());
//        }
//        return codeRanges;
//    }

//    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getInlinedOperation().getLocationInfo().getFilePath(), getInlinedOperation().getClassName()));
//        pairs.add(new ImmutablePair<String, String>(getTargetOperationBeforeInline().getLocationInfo().getFilePath(), getTargetOperationBeforeInline().getClassName()));
//        return pairs;
//    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getTargetOperationAfterInline().getLocationInfo().getFilePath(), getTargetOperationAfterInline().getClassName()));
//        return pairs;
//    }

//    @Override
//    public List<CodeRange> leftSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(getInlinedOperationCodeRange()
//                .setDescription("inlined method declaration")
//                .setCodeElement(inlinedOperation.toString()));
//        //ranges.add(getInlinedCodeRangeFromInlinedOperation().setDescription("inlined code from inlined method declaration"));
//        for(AbstractCodeFragment inlinedCodeFragment : inlinedCodeFragmentsFromInlinedOperation) {
//            ranges.add(inlinedCodeFragment.codeRange().setDescription("inlined code from inlined method declaration"));
//        }
//        ranges.add(getTargetOperationCodeRangeBeforeInline()
//                .setDescription("target method declaration before inline")
//                .setCodeElement(targetOperationBeforeInline.toString()));
//        for(OperationInvocation invocation : inlinedOperationInvocations) {
//            ranges.add(invocation.codeRange()
//                    .setDescription("inlined method invocation")
//                    .setCodeElement(invocation.actualString()));
//        }
//        for(StatementObject statement : bodyMapper.getNonMappedLeavesT1()) {
//            ranges.add(statement.codeRange().
//                    setDescription("deleted statement in inlined method declaration"));
//        }
//        for(CompositeStatementObject statement : bodyMapper.getNonMappedInnerNodesT1()) {
//            ranges.add(statement.codeRange().
//                    setDescription("deleted statement in inlined method declaration"));
//        }
//        return ranges;
//    }
//
//    @Override
//    public List<CodeRange> rightSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(getTargetOperationCodeRangeAfterInline()
//                .setDescription("target method declaration after inline")
//                .setCodeElement(targetOperationAfterInline.toString()));
//        for(AbstractCodeFragment inlinedCodeFragment : inlinedCodeFragmentsInTargetOperation) {
//            ranges.add(inlinedCodeFragment.codeRange().setDescription("inlined code in target method declaration"));
//        }
//        return ranges;
//    }
}
