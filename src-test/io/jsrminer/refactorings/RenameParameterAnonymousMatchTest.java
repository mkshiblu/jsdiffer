package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameParameterAnonymousMatchTest extends BaseTest {

    static List<IRefactoring> refactorings;
    static ExtractOperationRefactoring extractOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\RenameParameterAnonymousMatch.js"
                , getRootResourceDirectory() + "src2\\RenameParameterAnonymousMatch.js");

        IRefactoring refactoring = refactorings.get(0);
        assertEquals(RefactoringType.EXTRACT_OPERATION, refactoring.getRefactoringType());
        extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
    }

    @Test
    void testRefactoringsCount() {
        assertEquals(4, refactorings.size());
    }

}
