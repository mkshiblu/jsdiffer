package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddParameterRefactoringTest extends BaseTest {
    static List<IRefactoring> refactorings;
    static AddParameterRefactoring addParameterRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\AddParameter.js"
                , getRootResourceDirectory() + "src2\\AddParameter.js");

        addParameterRefactoring = (AddParameterRefactoring) refactorings.get(0);

    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testOperationAfter() {
        assertEquals("f1", addParameterRefactoring.getOperationAfter().getName());
    }

    @Test
    void testOperationBefore() {
        assertEquals("f1", addParameterRefactoring.getOperationBefore().getName());
    }

    @Test
    void testParameterName() {
        assertEquals("p1", addParameterRefactoring.getParameter().name);
    }
}
