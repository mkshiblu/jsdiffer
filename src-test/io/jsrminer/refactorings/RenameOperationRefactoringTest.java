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
        assertEquals(5, refactorings.size());
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

        assertEquals("let z = 10;", first.fragment1.getText());
        assertEquals("let z = 5;", first.fragment2.getText());
    }

    @Test
    void testReplacementsCount() {
        Iterator<CodeFragmentMapping> iterator = renameOperationRefactoring.getBodyMapper().getMappings().iterator();
        CodeFragmentMapping first = iterator.next();

        assertEquals(1, first.getReplacements().size());
    }

    @Test
    void testNestedFunctionMappings() {
        var iterator = renameOperationRefactoring.getBodyMapper().getNestedFunctionDeclrationMappings().iterator();
        FunctionBodyMapper first = iterator.next();
        assertEquals("m1.d", first.function1.getQualifiedName());
        assertEquals("m2.d", first.function2.getQualifiedName());
    }

    @Test
    void testReplacements() {
        Iterator<CodeFragmentMapping> iterator = renameOperationRefactoring.getBodyMapper().getMappings().iterator();
        CodeFragmentMapping first = iterator.next();

        Replacement replacement = first.getReplacements().iterator().next();

        assertEquals("10", replacement.getBefore());
        assertEquals("5", replacement.getAfter());
        assertEquals(ReplacementType.NUMBER_LITERAL, replacement.getType());
    }
}
