package io.jsrminer.refactorings;

import io.rminerx.core.api.IClassDeclaration;

public class RenameClassRefactoring extends Refactoring {
    private IClassDeclaration originalClass;
    private IClassDeclaration renamedClass;

    public RenameClassRefactoring(IClassDeclaration originalClass,  IClassDeclaration renamedClass) {
        this.originalClass = originalClass;
        this.renamedClass = renamedClass;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(originalClass.getName());
        sb.append(" renamed to ");
        sb.append(renamedClass.getName());
        return sb.toString();
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public RefactoringType getRefactoringType() {
        return RefactoringType.RENAME_CLASS;
    }

    public String getOriginalClassName() {
        return originalClass.getName();
    }

    public String getRenamedClassName() {
        return renamedClass.getName();
    }

    public IClassDeclaration getOriginalClass() {
        return originalClass;
    }

    public IClassDeclaration getRenamedClass() {
        return renamedClass;
    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getOriginalClass().getLocationInfo().getFilePath(), getOriginalClass().getName()));
//        return pairs;
//    }
//
//    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
//        Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<ImmutablePair<String, String>>();
//        pairs.add(new ImmutablePair<String, String>(getRenamedClass().getLocationInfo().getFilePath(), getRenamedClass().getName()));
//        return pairs;
//    }
//
//    @Override
//    public List<CodeRange> leftSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(originalClass.codeRange()
//                .setDescription("original type declaration")
//                .setCodeElement(originalClass.getName()));
//        return ranges;
//    }
//
//    @Override
//    public List<CodeRange> rightSide() {
//        List<CodeRange> ranges = new ArrayList<CodeRange>();
//        ranges.add(renamedClass.codeRange()
//                .setDescription("renamed type declaration")
//                .setCodeElement(renamedClass.getName()));
//        return ranges;
//    }
}
