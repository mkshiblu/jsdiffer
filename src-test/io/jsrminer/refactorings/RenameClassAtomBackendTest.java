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

public class RenameClassAtomBackendTest extends BaseTest {
    static RenameClassRefactoring renameClassRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\RenameClassAtomBackend.js",
                getRootResourceDirectory() + "src2\\RenameClassAtomBackend.js");
        renameClassRefactoring = (RenameClassRefactoring) refactorings.get(0);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(5, refactorings.size());
    }


    @Test
    void testMappingsTexts() {
        Set<CodeFragmentMapping> mappings = null;//renameClassRefactoring.getBodyMapper().getMappings();
        Iterator<CodeFragmentMapping> iterator = mappings.iterator();
        CodeFragmentMapping first = iterator.next();
        CodeFragmentMapping second = iterator.next();

        assertTrue(second.fragment1.getText().startsWith("return hyphenate"));
        assertTrue(second.fragment2.getText().startsWith("return hyphenate"));

        assertEquals("let z = 10;", first.fragment1.getText());
        assertEquals("let z = 5;", first.fragment2.getText());
    }
}
