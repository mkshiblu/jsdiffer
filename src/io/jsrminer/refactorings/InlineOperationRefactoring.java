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
        super(RefactoringType.INLINE_OPERATION);
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
        //if (getRefactoringType().equals(RefactoringType.INLINE_OPERATION)) {
        sb.append(" inlined to ");
        sb.append(targetOperationAfterInline);
        sb.append(" in class ");
        //sb.append(getClassName());
//        } else if (getRefactoringType().equals(RefactoringType.MOVE_AND_INLINE_OPERATION)) {
//            sb.append(" moved from class ");
        //sb.append(inlinedOperation.getClassName());
//            sb.append(" to class ");
//            sb.append(getTargetOperationAfterInline().getClassName());
//            sb.append(" & inlined to ");
//            sb.append(getTargetOperationAfterInline());
//        }
        return sb.toString();
    }
//
//    private String getClassName() {
//        return targetOperationAfterInline.getClassName();
//    }
//
//    public String getName() {
//        return this.getRefactoringType().getDisplayName();
//    }
//
//    public RefactoringType getRefactoringType() {
//        if (!getTargetOperationBeforeInline().getClassName().equals(getInlinedOperation().getClassName()))
//            return RefactoringType.MOVE_AND_INLINE_OPERATION;
//        return RefactoringType.INLINE_OPERATION;
//    }

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
}
