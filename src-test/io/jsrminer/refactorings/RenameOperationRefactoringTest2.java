package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

public class RenameOperationRefactoringTest2 extends BaseTest {
    static RenameOperationRefactoring renameOperationRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\RenameFunctionAddParameter.js",
                getRootResourceDirectory() + "src2\\RenameFunctionAddParameter.js");
        //renameOperationRefactoring = (RenameOperationRefactoring) refactorings.get(0);
    }

//    @Test
//    void testRefactoringCount() {
//        assertEquals(5, refactorings.size());
//    }

//    @Test
//    void testMapperMappingsCount() {
//        assertEquals(7, renameOperationRefactoring.getBodyMapper().getMappings().size());
//    }
//
//    @Test
//    void testMappingsTexts() {
//        Set<CodeFragmentMapping> mappings = renameOperationRefactoring.getBodyMapper().getMappings();
//        Iterator<CodeFragmentMapping> iterator = mappings.iterator();
//        CodeFragmentMapping first = iterator.next();
//        CodeFragmentMapping second = iterator.next();
//
//        assertTrue(second.fragment1.getText().startsWith("return hyphenate"));
//        assertTrue(second.fragment2.getText().startsWith("return hyphenate"));
//
//        assertEquals("let z = 10;", first.fragment1.getText());
//        assertEquals("let z = 5;", first.fragment2.getText());
//    }
//
//    @Test
//    void testReplacementsCount() {
//        Iterator<CodeFragmentMapping> iterator = renameOperationRefactoring.getBodyMapper().getMappings().iterator();
//        CodeFragmentMapping first = iterator.next();
//        CodeFragmentMapping second = iterator.next();
//
//        assertEquals(1, first.getReplacements().size());
//        assertEquals(0, second.getReplacements().size());
//    }
//
//    @Test
//    void testReplacements() {
//        Iterator<CodeFragmentMapping> iterator = renameOperationRefactoring.getBodyMapper().getMappings().iterator();
//        CodeFragmentMapping first = iterator.next();
//        CodeFragmentMapping second = iterator.next();
//
//        Replacement replacement = first.getReplacements().iterator().next();
//
//        assertEquals("10", replacement.getBefore());
//        assertEquals("5", replacement.getAfter());
//        assertEquals(ReplacementType.NUMBER_LITERAL, replacement.getType());
//    }
}
