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
        this.bodyMapper = bodyMapper;
        this.extractedOperation = bodyMapper.getOperation2();
        this.sourceOperationBeforeExtraction = bodyMapper.getOperation1();
        this.sourceOperationAfterExtraction = sourceOperationAfterExtraction;
        this.extractedOperationInvocations = operationInvocations;
        this.replacements = bodyMapper.getReplacements();
        this.extractedCodeFragmentsFromSourceOperation = new LinkedHashSet<>();
        this.extractedCodeFragmentsToExtractedOperation = new LinkedHashSet<>();
        for (CodeFragmentMapping mapping : bodyMapper.getMappings()) {
            this.extractedCodeFragmentsFromSourceOperation.add(mapping.getFragment1());
            this.extractedCodeFragmentsToExtractedOperation.add(mapping.getFragment2());
        }
    }

    public ExtractOperationRefactoring(FunctionBodyMapper bodyMapper, FunctionDeclaration extractedOperation,
                                       FunctionDeclaration sourceOperationBeforeExtraction, FunctionDeclaration sourceOperationAfterExtraction, List<OperationInvocation> operationInvocations) {
        this.bodyMapper = bodyMapper;
        this.extractedOperation = extractedOperation;
        this.sourceOperationBeforeExtraction = sourceOperationBeforeExtraction;
        this.sourceOperationAfterExtraction = sourceOperationAfterExtraction;
        this.extractedOperationInvocations = operationInvocations;
        this.replacements = bodyMapper.getReplacements();
        this.extractedCodeFragmentsFromSourceOperation = new LinkedHashSet<>();
        this.extractedCodeFragmentsToExtractedOperation = new LinkedHashSet<>();
        for (CodeFragmentMapping mapping : bodyMapper.getMappings()) {
            this.extractedCodeFragmentsFromSourceOperation.add(mapping.getFragment1());
            this.extractedCodeFragmentsToExtractedOperation.add(mapping.getFragment2());
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(extractedOperation);
        sb.append(" extracted from ");
        sb.append(sourceOperationBeforeExtraction);
        sb.append(" in class ");
        sb.append(getClassName());
        if (getRefactoringType().equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)) {
            sb.append(" & moved to ");
            sb.append(extractedOperation.getFullyQualifiedName());
        }
        return sb.toString();
    }

    private String getClassName() {
        if (getRefactoringType().equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)) {
            return getSourceOperationBeforeExtraction().getParentContainerQualifiedName();
        }
        String sourceClassName = getSourceOperationBeforeExtraction().getParentContainerQualifiedName();
        String targetClassName = getSourceOperationAfterExtraction().getParentContainerQualifiedName();
        return sourceClassName.equals(targetClassName) ? sourceClassName : targetClassName;
    }

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

    public Set<CodeFragment> getExtractedCodeFragmentsFromSourceOperation() {
        return extractedCodeFragmentsFromSourceOperation;
    }

    public Set<CodeFragment> getExtractedCodeFragmentsToExtractedOperation() {
        return extractedCodeFragmentsToExtractedOperation;
    }

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

//    /**
//     * @return the code range(s) of the invocation(s) to the extracted method inside the source method in the <b>child</b> commit
//     */
//    public Set<CodeRange> getExtractedOperationInvocationCodeRanges() {
//        Set<CodeRange> codeRanges = new LinkedHashSet<CodeRange>();
//        for(OperationInvocation invocation : extractedOperationInvocations) {
//            codeRanges.add(invocation.codeRange());
//        }
//        return codeRanges;
//    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public RefactoringType getRefactoringType() {
        if (!getSourceOperationAfterExtraction().getParentContainerQualifiedName().equals(getExtractedOperation().getParentContainerQualifiedName()))
            return RefactoringType.EXTRACT_AND_MOVE_OPERATION;
        return RefactoringType.EXTRACT_OPERATION;
    }

//    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getSourceOperationBeforeExtraction().getLocationInfo().getFilePath(), getSourceOperationBeforeExtraction().getClassName()));
//        return pairs;
//    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getSourceOperationAfterExtraction().getLocationInfo().getFilePath(), getSourceOperationAfterExtraction().getClassName()));
//        pairs.add(new ImmutablePair<String, String>(getExtractedOperation().getLocationInfo().getFilePath(), getExtractedOperation().getClassName()));
//        return pairs;
//    }

//    @Override
//    public List<CodeRange> leftSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(getSourceOperationCodeRangeBeforeExtraction()
//                .setDescription("source method declaration before extraction")
//                .setCodeElement(sourceOperationBeforeExtraction.toString()));
//        for(AbstractCodeFragment extractedCodeFragment : extractedCodeFragmentsFromSourceOperation) {
//            ranges.add(extractedCodeFragment.codeRange().setDescription("extracted code from source method declaration"));
//        }
//		/*
//		CodeRange extractedCodeRangeFromSourceOperation = getExtractedCodeRangeFromSourceOperation();
//		ranges.add(extractedCodeRangeFromSourceOperation.setDescription("extracted code from source method declaration"));
//		for(StatementObject statement : bodyMapper.getNonMappedLeavesT1()) {
//			if(extractedCodeRangeFromSourceOperation.subsumes(statement.codeRange())) {
//				ranges.add(statement.codeRange().
//						setDescription("deleted statement in source method declaration"));
//			}
//		}
//		for(CompositeStatementObject statement : bodyMapper.getNonMappedInnerNodesT1()) {
//			if(extractedCodeRangeFromSourceOperation.subsumes(statement.codeRange()) ||
//					extractedCodeRangeFromSourceOperation.subsumes(statement.getLeaves())) {
//				ranges.add(statement.codeRange().
//						setDescription("deleted statement in source method declaration"));
//			}
//		}
//		*/
//        return ranges;
//    }
//
//    @Override
//    public List<CodeRange> rightSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(getExtractedOperationCodeRange()
//                .setDescription("extracted method declaration")
//                .setCodeElement(extractedOperation.toString()));
//        //ranges.add(getExtractedCodeRangeToExtractedOperation().setDescription("extracted code to extracted method declaration"));
//        for(AbstractCodeFragment extractedCodeFragment : extractedCodeFragmentsToExtractedOperation) {
//            ranges.add(extractedCodeFragment.codeRange().setDescription("extracted code to extracted method declaration"));
//        }
//        ranges.add(getSourceOperationCodeRangeAfterExtraction()
//                .setDescription("source method declaration after extraction")
//                .setCodeElement(sourceOperationAfterExtraction.toString()));
//        for(OperationInvocation invocation : extractedOperationInvocations) {
//            ranges.add(invocation.codeRange()
//                    .setDescription("extracted method invocation")
//                    .setCodeElement(invocation.actualString()));
//        }
//        for(StatementObject statement : bodyMapper.getNonMappedLeavesT2()) {
//            ranges.add(statement.codeRange().
//                    setDescription("added statement in extracted method declaration"));
//        }
//        for(CompositeStatementObject statement : bodyMapper.getNonMappedInnerNodesT2()) {
//            ranges.add(statement.codeRange().
//                    setDescription("added statement in extracted method declaration"));
//        }
//        return ranges;
//    }
}
