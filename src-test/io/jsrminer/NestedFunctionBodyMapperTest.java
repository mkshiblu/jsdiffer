package io.jsrminer;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class NestedFunctionBodyMapperTest extends BaseTest {
    static RenameOperationRefactoring renameOperationRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\NestedFunctionBodyMapper.js",
                getRootResourceDirectory() + "src2\\NestedFunctionBodyMapper.js");
        renameOperationRefactoring = (RenameOperationRefactoring) refactorings.get(0);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testMapperMappingsCount() {
        assertEquals(0, renameOperationRefactoring.getBodyMapper().getMappings().size());
    }

    @Test
    void testNestedFunctionsMappingsCount() {
        assertEquals(1, renameOperationRefactoring.getBodyMapper().getNestedFunctionDeclrationMappings().size());
        assertEquals(0, renameOperationRefactoring.getBodyMapper().getNonMappedNestedFunctionDeclrationsT1().size());
        assertEquals(0, renameOperationRefactoring.getBodyMapper().getNonMappedNestedFunctionDeclrationsT2().size());
    }

    @Test
    void testNestedFunctionMappingsName() {
        var nestedMapper = renameOperationRefactoring.getBodyMapper().getNestedFunctionDeclrationMappings();

        var iterator = nestedMapper.iterator();
        var first = iterator.next();

        assertTrue("d".equals(first.function1.getName()));
        assertTrue("d".equals(first.function2.getName()));
    }

    @Test
    void testReplacementsCount() {
        Iterator<CodeFragmentMapping> iterator = renameOperationRefactoring.getBodyMapper().getMappings().iterator();
        assertFalse(iterator.hasNext());
    }
}
