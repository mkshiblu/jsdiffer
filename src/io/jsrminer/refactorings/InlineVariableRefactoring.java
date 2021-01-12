package io.jsrminer.refactorings;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.mapping.CodeFragmentMapping;

import java.util.LinkedHashSet;
import java.util.Set;

public class InlineVariableRefactoring extends Refactoring {
    private VariableDeclaration variableDeclaration;
    private FunctionDeclaration operationBefore;
    private FunctionDeclaration operationAfter;
    private Set<CodeFragmentMapping> references;

    public InlineVariableRefactoring(VariableDeclaration variableDeclaration, FunctionDeclaration operationBefore, FunctionDeclaration operationAfter) {
        this.variableDeclaration = variableDeclaration;
        this.operationBefore = operationBefore;
        this.operationAfter = operationAfter;
        this.references = new LinkedHashSet<>();
    }

    public void addReference(CodeFragmentMapping mapping) {
        references.add(mapping);
    }

    public RefactoringType getRefactoringType() {
        return RefactoringType.INLINE_VARIABLE;
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public VariableDeclaration getVariableDeclaration() {
        return variableDeclaration;
    }

    public FunctionDeclaration getOperationBefore() {
        return operationBefore;
    }

    public FunctionDeclaration getOperationAfter() {
        return operationAfter;
    }

    public Set<CodeFragmentMapping> getReferences() {
        return references;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(variableDeclaration);
        sb.append(" in method ");
        sb.append(operationBefore);
        sb.append(" at ");
        sb.append(operationBefore.getFullyQualifiedName());
        return sb.toString();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operationBefore == null) ? 0 : operationBefore.hashCode());
        result = prime * result + ((variableDeclaration == null) ? 0 : variableDeclaration.hashCode());
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
        InlineVariableRefactoring other = (InlineVariableRefactoring) obj;
        if (operationBefore == null) {
            if (other.operationBefore != null)
                return false;
        } else if (!operationBefore.equals(other.operationBefore))
            return false;
        if (variableDeclaration == null) {
            if (other.variableDeclaration != null)
                return false;
        } else if (!variableDeclaration.equals(other.variableDeclaration))
            return false;
        return true;
    }

//    /**
//     * @return the code range of the inlined variable declaration in the <b>parent</b> commit
//     */
//    public CodeRange getInlinedVariableDeclarationCodeRange() {
//        return variableDeclaration.codeRange();
//    }

//    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOperationBefore().getLocationInfo().getFilePath(), getOperationBefore().getClassName()));
//        return pairs;
//    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOperationAfter().getLocationInfo().getFilePath(), getOperationAfter().getClassName()));
//        return pairs;
//    }

//    @Override
//    public List<CodeRange> leftSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(variableDeclaration.codeRange()
//                .setDescription("inlined variable declaration")
//                .setCodeElement(variableDeclaration.toString()));
//        for(AbstractCodeMapping mapping : references) {
//            ranges.add(mapping.getFragment1().codeRange().setDescription("statement with the name of the inlined variable"));
//        }
//        return ranges;
//    }
//
//    @Override
//    public List<CodeRange> rightSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        for(AbstractCodeMapping mapping : references) {
//            ranges.add(mapping.getFragment2().codeRange().setDescription("statement with the initializer of the inlined variable"));
//        }
//        return ranges;
//    }
}
