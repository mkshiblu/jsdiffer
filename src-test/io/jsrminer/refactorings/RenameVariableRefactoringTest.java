package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameVariableRefactoringTest extends BaseTest {
    static List<IRefactoring> refactorings;
    static RenameVariableRefactoring renameVariableRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\RenameVariable.js"
                , getRootResourceDirectory() + "src2\\RenameVariable.js");
        renameVariableRefactoring = (RenameVariableRefactoring) refactorings.get(1);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testOriginalVariableName() {
        assertEquals("keyCodes", renameVariableRefactoring.getOriginalVariable().variableName);
    }

    @Test
    void testRenamedVariableName() {
        assertEquals("mappedKeyCode", renameVariableRefactoring.getRenamedVariable().variableName);
    }
}
