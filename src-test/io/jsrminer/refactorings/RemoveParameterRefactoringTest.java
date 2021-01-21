package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RemoveParameterRefactoringTest extends BaseTest {
    static List<IRefactoring> refactorings;
    static RemoveParameterRefactoring removeParameterRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src2\\AddParameter.js"
                , getRootResourceDirectory() + "src1\\AddParameter.js");

        removeParameterRefactoring = (RemoveParameterRefactoring) refactorings.get(0);

    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testOperationAfter() {
        assertEquals("f1", removeParameterRefactoring.getOperationAfter().getName());
    }

    @Test
    void testOperationBefore() {
        assertEquals("f1", removeParameterRefactoring.getOperationBefore().getName());
    }

    @Test
    void testParameterName() {
        assertEquals("p1", removeParameterRefactoring.getParameter().name);
    }
}
