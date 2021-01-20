package io.jsrminer.refactorings;

import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.TestBase;
import io.jsrminer.api.IRefactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides various data such as refactorings to be tested
 */
public class RefactoringDataProvider {
    final static List<IRefactoring> refactorings = new ArrayList<>();
    final static List<IRefactoring> refactoringsReversed = new ArrayList<>();

    static {
        JSRefactoringMiner rminer = new JSRefactoringMiner();
//        refactorings = rminer.detectBetweenDirectories(TestBase.getRootResourceDirectory() + "ExtractOrInlineFunction\\src1"
//                , TestBase.getRootResourceDirectory() + "ExtractOrInlineFunction\\src2");
//
//        refactoringsReversed = rminer.detectBetweenDirectories(TestBase.getRootResourceDirectory() + "ExtractOrInlineFunction\\src2"
//                , TestBase.getRootResourceDirectory() + "ExtractOrInlineFunction\\src1");

        refactorings.addAll(rminer.detectBetweenDirectories(TestBase.getRootResourceDirectory() + "src1"
                , TestBase.getRootResourceDirectory() + "src2"));

        refactoringsReversed.addAll(rminer.detectBetweenDirectories(TestBase.getRootResourceDirectory() + "src2"
                , TestBase.getRootResourceDirectory() + "src1"));
    }

    public static List<IRefactoring> getRefactoringsInReverse() {
        return refactoringsReversed;
    }

    public static List<IRefactoring> getRefactoringsInReverseOfType(RefactoringType type) {
        return refactoringsReversed.stream().filter(r -> r.getRefactoringType() == type).collect(Collectors.toList());
    }
//
//    public static List<IRefactoring> getExtractMethodRefactorings() {
//        return refactorings.stream().filter(r -> r.getRefactoringType() == RefactoringType.EXTRACT_OPERATION).collect(Collectors.toList());
//    }
//
//    public static List<IRefactoring> getInlineMethodRefactorings() {
//        return refactorings.stream().filter(r -> r.getRefactoringType() == RefactoringType.INLINE_OPERATION).collect(Collectors.toList());
//    }
//
//    public static List<IRefactoring> getRenameMethodRefactorings() {
//        return refactorings.stream().filter(r -> r.getRefactoringType() == RefactoringType.RENAME_METHOD).collect(Collectors.toList());
//    }
//
//    public static List<IRefactoring> getRenameVariableRefactorings() {
//        return refactorings.stream().filter(r -> r.getRefactoringType() == RefactoringType.RENAME_VARIABLE).collect(Collectors.toList());
//    }

    public static List<IRefactoring> getRefactoringsOfType(RefactoringType refactoringType) {
        return refactorings.stream().filter(r -> r.getRefactoringType() == refactoringType).collect(Collectors.toList());
    }
}

