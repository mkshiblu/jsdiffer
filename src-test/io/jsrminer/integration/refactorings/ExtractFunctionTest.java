package io.jsrminer.integration.refactorings;

import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.refactorings.IRefactoring;
import io.jsrminer.refactorings.RefactoringType;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExtractFunctionTest {

    @Test
    void name() {
        assertTrue("x".length() == 1);
    }

    @Test
    void extractCreateAddressTest() {
        final String testSrcPath = "test-resources\\";

        List<IRefactoring> refactorings = new JSRefactoringMiner().detectBetweenDirectories(testSrcPath + "ExtractOrInlineFunction\\src1"
                , testSrcPath + "ExtractOrInlineFunction\\src2");

        assertNotNull(refactorings);
        assertTrue(refactorings.size() == 1);

        IRefactoring refactoring = refactorings.get(0);
        assertEquals(RefactoringType.EXTRACT_OPERATION, refactoring.getRefactoringType());

        ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
        assertEquals(6, extractOperationRefactoring.getBodyMapper().getMappings().size());

        assertEquals(0, extractOperationRefactoring.getReplacements().size());

        // UnmatchedCounts
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();
        assertEquals(1, mapper.getNonMappedLeavesT1().size());
        assertEquals(1, mapper.getNonMappedLeavesT2().size());
        assertEquals(2, mapper.getNonMappedInnerNodesT1().size());
        assertEquals(0, mapper.getNonMappedInnerNodesT2().size());
    }
}
