package io.jsrminer.refactorings;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenameOperationInsideFunctionExpressionTest extends BaseTest {
    static RenameOperationRefactoring renameOperationRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\renameinsidefunctionexpressionassignment.js",
                getRootResourceDirectory() + "src2\\renameinsidefunctionexpressionassignment.js");
        renameOperationRefactoring = (RenameOperationRefactoring) refactorings.get(0);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }
    @Test
    void testOriginalOperation() {
        assertEquals("getValueOrDefault", renameOperationRefactoring.getOriginalOperation().getName());
    }
    @Test
    void testRenamedOperation() {
        assertEquals("valueOrDefault", renameOperationRefactoring.getRenamedOperation().getName());
    }

    @Test
    void testMapperMappingsCount() {
        assertEquals(1, renameOperationRefactoring.getBodyMapper().getMappings().size());
    }

    @Test
    void testMappingsTexts() {
        Set<CodeFragmentMapping> mappings = renameOperationRefactoring.getBodyMapper().getMappings();
        Iterator<CodeFragmentMapping> iterator = mappings.iterator();
        CodeFragmentMapping first = iterator.next();

        assertEquals("return typeof value === 'undefined' ? defaultValue : value;", first.fragment1.getText());
        assertEquals("return typeof value === 'undefined' ? defaultValue : value;", first.fragment2.getText());
    }

    @Test
    void testReplacementsCount() {
        Iterator<CodeFragmentMapping> iterator = renameOperationRefactoring.getBodyMapper().getMappings().iterator();
        CodeFragmentMapping first = iterator.next();

        assertEquals(0, first.getReplacements().size());
    }
}
