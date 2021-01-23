package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoveFunctionRefactoringTest extends BaseTest {
    static List<IRefactoring> refactorings;
    static MoveOperationRefactoring moveOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "src1"
                , getRootResourceDirectory() + "src2");
       // moveOperationRefactoring = (MoveOperationRefactoring) refactorings.stream().filter(r -> r.getRefactoringType().equals(RefactoringType.MOVE_OPERATION)).findFirst().orElse(null);
    }

    @Test
    void testMovedOperation() {
        assertEquals("f1", moveOperationRefactoring.movedOperation.getName());
    }
//
//    @Test
//    void testOriginalOperation() {
//        assertEquals("f1", moveOperationRefactoring.originalOperation.getName());
//    }
//
//    @Test
//    void testOriginalFile() {
//        assertEquals("MoveOperation1", moveOperationRefactoring.originalOperation.getParentContainerQualifiedName());
//    }
//
//    @Test
//    void testTargetFile() {
//        assertEquals("MoveOperation2", moveOperationRefactoring.movedOperation.getParentContainerQualifiedName());
//    }
}
