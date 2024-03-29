package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoveFunctionToAddedFilesTest extends BaseTest {
    static List<IRefactoring> refactorings;
    static MoveAndRenameFunctionRefactoring refactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "src1\\MoveFunctionToAdded"
                , getRootResourceDirectory() + "src2\\MoveFunctionToAdded");
       refactoring = (MoveAndRenameFunctionRefactoring) refactorings.get(1);
    }

    @Test
    void testMovedFile() {
        assertEquals("same_content2.js", refactoring.getMovedFile().getName());
    }

//    @Test
//    void testMovedFilePath() {
//        assertEquals("mr2", refactoring.getMovedFile().getDirectoryName());
//    }
}
