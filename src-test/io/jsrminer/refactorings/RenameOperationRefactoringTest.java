package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenameOperationRefactoringTest extends BaseTest {
    static RenameOperationRefactoring renameOperationRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\RenameFunction.js",
                getRootResourceDirectory() + "src2\\RenameFunction.js");
        renameOperationRefactoring = (RenameOperationRefactoring) refactorings.get(0);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testMapperMappingsCount() {
        assertEquals(2, renameOperationRefactoring.getBodyMapper().getMappings().size());
    }

    @Test
    void testMappingsTexts() {
        Set<CodeFragmentMapping> mappings = renameOperationRefactoring.getBodyMapper().getMappings();
        Iterator<CodeFragmentMapping> iterator = mappings.iterator();
        CodeFragmentMapping first = iterator.next();
        CodeFragmentMapping second = iterator.next();


        assertTrue(first.fragment1.getText().startsWith("let d ="));
        assertTrue(first.fragment2.getText().startsWith("let d ="));

        assertEquals("let z = 10;", second.fragment1.getText());
        assertEquals("let z = 5;", second.fragment2.getText());
    }

    @Test
    void testReplacementsCount() {
        Iterator<CodeFragmentMapping> iterator = renameOperationRefactoring.getBodyMapper().getMappings().iterator();
        CodeFragmentMapping first = iterator.next();
        CodeFragmentMapping second = iterator.next();

        assertEquals(0, first.getReplacements().size());
        assertEquals(1, second.getReplacements().size());
    }

    @Test
    void testReplacements() {
        Iterator<CodeFragmentMapping> iterator = renameOperationRefactoring.getBodyMapper().getMappings().iterator();
        CodeFragmentMapping first = iterator.next();
        CodeFragmentMapping second = iterator.next();

        Replacement replacement = second.getReplacements().iterator().next();

        assertEquals("10", replacement.getBefore());
        assertEquals("5", replacement.getAfter());
        assertEquals(ReplacementType.NUMBER_LITERAL, replacement.getType());
    }
}
