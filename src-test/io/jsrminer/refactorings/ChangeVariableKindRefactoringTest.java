package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeVariableKindRefactoringTest extends BaseTest {
    static String code1 = """
               function f1() {
                    const x = 4;
               }
            """;

    static String code2;

    static {
        code2 = """
                   function f1() {
                        let y = 5;
                   }
                """;
    }

    static List<IRefactoring> refactorings;
    static ChangeVariableKindRefactoring changeKindRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenCodeSnippets("snippet1.js"
                , code1, "snippet1.js", code2);
        changeKindRefactoring = (ChangeVariableKindRefactoring) refactorings.get(0);
    }


    @Test
    void testRefactoringCount() {
        assertEquals(2, refactorings.size());
    }

    @Test
    void testOriginalVariableKind() {
        assertEquals("const", changeKindRefactoring.getOriginalVariableKind());
    }

    @Test
    void testNewVariableKind() {
        assertEquals("let", changeKindRefactoring.getNewVariableKind());
    }

    @Test
    void testOriginalVariableName() {
        assertEquals("x", changeKindRefactoring.getOriginalVariable().variableName);
    }

    @Test
    void testNewVariableName() {
        assertEquals("y", changeKindRefactoring.getChangedTypeVariable().variableName);
    }
}
