package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameFunctionAtomReplaceTest extends BaseTest {

    static List<IRefactoring> refactorings;
    static RenameOperationRefactoring renameReplaceRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\RenameFunctionReplace_Atom.js"
                , getRootResourceDirectory() + "src2\\RenameFunctionReplace_Atom.js");

        IRefactoring refactoring = refactorings.get(0);
        renameReplaceRefactoring = (RenameOperationRefactoring) refactoring;
    }

    @Test
    void testRefactoringsCount() {
        assertEquals(3, refactorings.size());
    }

    @Test
    void testRefactoringType() {
        assertEquals(RefactoringType.RENAME_METHOD, renameReplaceRefactoring.getRefactoringType());
    }

    @Test
    void testFunctionNamesReplace() {
        assertEquals("replace", renameReplaceRefactoring.getRenamedOperation().getName());
        assertEquals("replaceAtomProject", renameReplaceRefactoring.getOriginalOperation().getName());


    }

    @Test
    void testFunctionNamesDidReplace() {
        RenameOperationRefactoring renameDidReplaceRefactoring = (RenameOperationRefactoring)  refactorings.get(1);
        assertEquals("onDidReplace", renameDidReplaceRefactoring.getRenamedOperation().getName());
        assertEquals("onDidReplaceAtomProject", renameDidReplaceRefactoring.getOriginalOperation().getName());
    }

    @Test
    void testFunctionNamesGetAtomProjectFilePath() {
        RenameOperationRefactoring renameDidReplaceRefactoring = (RenameOperationRefactoring)  refactorings.get(2);
        assertEquals("getProjectFilePath", renameDidReplaceRefactoring.getRenamedOperation().getName());
        assertEquals("getAtomProjectFilePath", renameDidReplaceRefactoring.getOriginalOperation().getName());
    }
}
