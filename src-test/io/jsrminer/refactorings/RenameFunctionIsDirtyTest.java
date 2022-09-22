package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameFunctionIsDirtyTest extends BaseTest {

    static List<IRefactoring> refactorings;
    static RenameOperationRefactoring renameOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\RenameFunctionMaterialIsDirty.js"
                , getRootResourceDirectory() + "src2\\RenameFunctionMaterialIsDirty.js");

        IRefactoring refactoring = refactorings.get(0);
        renameOperationRefactoring = (RenameOperationRefactoring) refactoring;
    }

    @Test
    void testRefactoringsCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testRefactoringType() {
        assertEquals(RefactoringType.RENAME_METHOD, renameOperationRefactoring.getRefactoringType());
    }

    @Test
    void testFunctionNames() {
        assertEquals("isEmpty", renameOperationRefactoring.getRenamedOperation().getName());
        assertEquals("isDirty", renameOperationRefactoring.getOriginalOperation().getName());
    }
}
