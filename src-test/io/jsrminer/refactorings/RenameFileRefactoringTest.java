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
    static RenameFileRefactoring moveFileRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "rename\\v1"
                , getRootResourceDirectory() + "rename\\v2");
        moveFileRefactoring = (RenameFileRefactoring) refactorings.get(0);
    }

    @Test
    void testRenamedFileName() {
        assertEquals("same_content.js", moveFileRefactoring.getRenamedFileName());
    }

    @Test
    void testRenamedFilePath() {
        assertEquals("v2/rename", moveFileRefactoring.getRenamedFile().getFilepath());
    }
}
