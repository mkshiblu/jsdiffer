package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameFunctionAtomReplaceTest extends BaseTest {

    static List<IRefactoring> refactorings;
    static RenameOperationRefactoring renameOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\RenameFunctionReplace_Atom.js"
                , getRootResourceDirectory() + "src2\\RenameFunctionReplace_Atom.js");

        IRefactoring refactoring = refactorings.get(0);
        renameOperationRefactoring = (RenameOperationRefactoring) refactoring;
    }

    @Test
    void testRefactoringsCount() {
        assertEquals(3, refactorings.size());
    }

    @Test
    void testRefactoringType() {
        assertEquals(RefactoringType.RENAME_METHOD, renameOperationRefactoring.getRefactoringType());
    }

    @Test
    void testFunctionNames() {
        assertEquals("getProjectFilePath", renameOperationRefactoring.getRenamedOperation().getName());
        assertEquals("getAtomProjectFilePath", renameOperationRefactoring.getOriginalOperation().getName());
    }
}
