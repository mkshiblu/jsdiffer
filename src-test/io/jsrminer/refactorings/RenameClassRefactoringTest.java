package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameClassRefactoringTest extends BaseTest {
    static RenameClassRefactoring renameClassRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "renameclass\\v1"
                , getRootResourceDirectory() + "renameclass\\v2");
        renameClassRefactoring = (RenameClassRefactoring) refactorings.get(1);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(2, refactorings.size());
    }

    @Test
    public void testRefactoringType() {
        assertEquals(RefactoringType.RENAME_CLASS, renameClassRefactoring.getRefactoringType());
    }

    @Test
    public void testRenamedClassName() {
        assertEquals("TouchableText", renameClassRefactoring.getRenamedClass().getName());
        assertEquals("TouchableText", renameClassRefactoring.getRenamedClass().getQualifiedName());

        assertEquals("Text.js", renameClassRefactoring.getRenamedClass().getParentContainerQualifiedName());
    }

    @Test
    public void testOriginalClassName() {
        assertEquals("Text", renameClassRefactoring.getOriginalClass().getName());
        assertEquals("Text", renameClassRefactoring.getOriginalClass().getQualifiedName());

        assertEquals("Text.js", renameClassRefactoring.getOriginalClass().getParentContainerQualifiedName());
    }
}
