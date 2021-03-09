package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoveFileRefactoringTest extends BaseTest {
    static List<IRefactoring> refactorings;
    static MoveFileRefactoring moveFileRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "move\\v1"
                , getRootResourceDirectory() + "move\\v2");
        moveFileRefactoring = (MoveFileRefactoring) refactorings.get(0);
    }

    @Test
    void testMovedFile() {
        assertEquals("same_content.js", moveFileRefactoring.getMovedFileName());
    }

    @Test
    void testMovedFilePath() {
        assertEquals("mv2", moveFileRefactoring.getMovedPathDirectory());
    }
}
