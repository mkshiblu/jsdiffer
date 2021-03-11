package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameFileRefactoringTest extends BaseTest {
    static List<IRefactoring> refactorings;
    static RenameFileRefactoring refactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "rename/v1"
                , getRootResourceDirectory() + "rename/v2");
        refactoring = (RenameFileRefactoring) refactorings.get(0);
    }

    @Test
    void testRenamedFileName() {
        assertEquals("errorCaptured.spec.js", refactoring.getRenamedFileName());
    }

    @Test
    void testRenamedFilePath() {
        assertEquals("rename\\errorCaptured.spec.js", refactoring.getRenamedFile().getFilepath());
    }

    @Test
    void testOriginalFileName() {
        assertEquals("catchError.spec.js", refactoring.getOriginalFileName());
    }

    @Test
    void testOriginalFilePath() {
        assertEquals("rename\\catchError.spec.js", refactoring.getOriginalFile().getFilepath());
    }
}
