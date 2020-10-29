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


public class ExtractOperationRefactoring extends Refactoring {
    private FunctionDeclaration extractedOperation;
    private FunctionDeclaration sourceOperationBeforeExtraction;
    private FunctionDeclaration sourceOperationAfterExtraction;
    private List<OperationInvocation> extractedOperationInvocations;
    private Set<Replacement> replacements;
    private Set<CodeFragment> extractedCodeFragmentsFromSourceOperation;
    private Set<CodeFragment> extractedCodeFragmentsToExtractedOperation;
    private FunctionBodyMapper bodyMapper;

    public ExtractOperationRefactoring(FunctionBodyMapper bodyMapper, FunctionDeclaration sourceOperationAfterExtraction, List<OperationInvocation> operationInvocations) {
        super(RefactoringType.EXTRACT_OPERATION);
        this.bodyMapper = bodyMapper;
        this.extractedOperation = bodyMapper.function2;
        this.sourceOperationBeforeExtraction = bodyMapper.function1;
        this.sourceOperationAfterExtraction = sourceOperationAfterExtraction;
        this.extractedOperationInvocations = operationInvocations;
        this.replacements = bodyMapper.getReplacements();
        this.extractedCodeFragmentsFromSourceOperation = new LinkedHashSet<>();
        this.extractedCodeFragmentsToExtractedOperation = new LinkedHashSet<>();
        for (CodeFragmentMapping mapping : bodyMapper.getMappings()) {
            this.extractedCodeFragmentsFromSourceOperation.add(mapping.fragment1);
            this.extractedCodeFragmentsToExtractedOperation.add(mapping.fragment2);
        }
    }

    public ExtractOperationRefactoring(FunctionBodyMapper bodyMapper
            , FunctionDeclaration extractedOperation
            , FunctionDeclaration sourceOperationBeforeExtraction
            , FunctionDeclaration sourceOperationAfterExtraction
            , List<OperationInvocation> operationInvocations) {
        super(RefactoringType.EXTRACT_OPERATION);
        this.bodyMapper = bodyMapper;
        this.extractedOperation = extractedOperation;
        this.sourceOperationBeforeExtraction = sourceOperationBeforeExtraction;
        this.sourceOperationAfterExtraction = sourceOperationAfterExtraction;
        this.extractedOperationInvocations = operationInvocations;
        this.replacements = bodyMapper.getReplacements();
        this.extractedCodeFragmentsFromSourceOperation = new LinkedHashSet<>();
        this.extractedCodeFragmentsToExtractedOperation = new LinkedHashSet<>();
        for (CodeFragmentMapping mapping : bodyMapper.getMappings()) {
            this.extractedCodeFragmentsFromSourceOperation.add(mapping.fragment1);
            this.extractedCodeFragmentsToExtractedOperation.add(mapping.fragment2);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(extractedOperation);
        sb.append(" extracted from ");
        sb.append(sourceOperationBeforeExtraction);
        sb.append(" in class ");
        //sb.append(getClassName());
//        if (getRefactoringType().equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)) {
//            sb.append(" & moved to class ");
//            sb.append(extractedOperation.getClassName());
//        }
        return sb.toString();
    }
//
//    private String getClassName() {
//        if (getRefactoringType().equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)) {
//            return getSourceOperationBeforeExtraction().getClassName();
//        }
//        String sourceClassName = getSourceOperationBeforeExtraction().getClassName();
//        String targetClassName = getSourceOperationAfterExtraction().getClassName();
//        return sourceClassName.equals(targetClassName) ? sourceClassName : targetClassName;
//    }

    public FunctionBodyMapper getBodyMapper() {
        return bodyMapper;
    }

    public FunctionDeclaration getExtractedOperation() {
        return extractedOperation;
    }

    public FunctionDeclaration getSourceOperationBeforeExtraction() {
        return sourceOperationBeforeExtraction;
    }

    public FunctionDeclaration getSourceOperationAfterExtraction() {
        return sourceOperationAfterExtraction;
    }

    public List<OperationInvocation> getExtractedOperationInvocations() {
        return extractedOperationInvocations;
    }

    public Set<Replacement> getReplacements() {
        return replacements;
    }

//    public Set<AbstractCodeFragment> getExtractedCodeFragmentsFromSourceOperation() {
//        return extractedCodeFragmentsFromSourceOperation;
//    }
//
//    public Set<AbstractCodeFragment> getExtractedCodeFragmentsToExtractedOperation() {
//        return extractedCodeFragmentsToExtractedOperation;
//    }
//
//    /**
//     * @return the code range of the source method in the <b>parent</b> commit
//     */
//    public CodeRange getSourceOperationCodeRangeBeforeExtraction() {
//        return sourceOperationBeforeExtraction.codeRange();
//    }
//
//    /**
//     * @return the code range of the source method in the <b>child</b> commit
//     */
//    public CodeRange getSourceOperationCodeRangeAfterExtraction() {
//        return sourceOperationAfterExtraction.codeRange();
//    }
//
//    /**
//     * @return the code range of the extracted method in the <b>child</b> commit
//     */
//    public CodeRange getExtractedOperationCodeRange() {
//        return extractedOperation.codeRange();
//    }
//
//    /**
//     * @return the code range of the extracted code fragment from the source method in the <b>parent</b> commit
//     */
//    public CodeRange getExtractedCodeRangeFromSourceOperation() {
//        return CodeRange.computeRange(extractedCodeFragmentsFromSourceOperation);
//    }
//
//    /**
//     * @return the code range of the extracted code fragment to the extracted method in the <b>child</b> commit
//     */
//    public CodeRange getExtractedCodeRangeToExtractedOperation() {
//        return CodeRange.computeRange(extractedCodeFragmentsToExtractedOperation);
//    }
//
//    /**
//     * @return the code range(s) of the invocation(s) to the extracted method inside the source method in the <b>child</b> commit
//     */
//    public Set<CodeRange> getExtractedOperationInvocationCodeRanges() {
//        Set<CodeRange> codeRanges = new LinkedHashSet<CodeRange>();
//        for (OperationInvocation invocation : extractedOperationInvocations) {
//            codeRanges.add(invocation.codeRange());
//        }
//        return codeRanges;
//    }

//    public RefactoringType getRefactoringType() {
//        if (!getSourceOperationAfterExtraction().getClassName().equals(getExtractedOperation().getClassName()))
//            return RefactoringType.EXTRACT_AND_MOVE_OPERATION;
//        return RefactoringType.EXTRACT_OPERATION;
//    }

}
