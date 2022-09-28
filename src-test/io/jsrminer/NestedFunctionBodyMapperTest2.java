package io.jsrminer;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NestedFunctionBodyMapperTest2 extends BaseTest {
    static RenameOperationRefactoring renameOperationRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\NestedFunctionBodyMapper2.js",
                getRootResourceDirectory() + "src2\\NestedFunctionBodyMapper2.js");
        renameOperationRefactoring = (RenameOperationRefactoring) refactorings.get(0);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(2, refactorings.size());
    }

    @Test
    void testMappingsCount() {
        assertEquals(1, renameOperationRefactoring.getBodyMapper().getMappings().size());
        assertEquals(2, renameOperationRefactoring.getBodyMapper().nonMappedLeafElementsT1());
        assertEquals(2, renameOperationRefactoring.getBodyMapper().nonMappedLeafElementsT2());
    }

    @Test
    void testNestedFunctionsMappingsCount() {
        assertEquals(2, renameOperationRefactoring.getBodyMapper().getMappedNestedFunctionDeclrations().size());
        assertEquals(1, renameOperationRefactoring.getBodyMapper().getNonMappedNestedFunctionDeclrationsT1().size());
        assertEquals(0, renameOperationRefactoring.getBodyMapper().getNonMappedNestedFunctionDeclrationsT2().size());
    }

    @Test
    void testNestedFunctionMappingsName() {
        var nestedMapper = renameOperationRefactoring.getBodyMapper().getMappedNestedFunctionDeclrations();

        var iterator = nestedMapper.iterator();
        var first = iterator.next();

        assertTrue("a".equals(first.function1.getName()));
        assertTrue("a".equals(first.function2.getName()));

        var second = iterator.next();
        assertTrue("b".equals(second.function1.getName()));
        assertTrue("c".equals(second.function2.getName()));
    }
}
