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
    static MoveFileToAnotherSourceFolderRefactoring moveOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "move\\v1"
                , getRootResourceDirectory() + "move\\v2");
        moveOperationRefactoring = (MoveFileToAnotherSourceFolderRefactoring) refactorings.get(0);
    }

    @Test
    void testMovedFile() {
        assertEquals("same_content.js", moveOperationRefactoring.getMovedClass().getName());
    }
//
//    @Test
//    void testOriginalOperation() {
//        assertEquals("mf2", moveOperationRefactoring.originalOperation.getName());
//    }
//
//    @Test
//    void testOriginalFile() {
//        assertEquals("dir\\dir1\\MoveFunction.js", moveOperationRefactoring.originalOperation.getSourceLocation().getFilePath());
//    }
//
//    @Test
//    void testTargetFile() {
//        assertEquals("dir\\dir1\\MoveTarget.js", moveOperationRefactoring.movedOperation.getSourceLocation().getFilePath());
//    }
}
