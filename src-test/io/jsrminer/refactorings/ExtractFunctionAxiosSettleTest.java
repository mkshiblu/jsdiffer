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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtractFunctionAxiosSettleTest extends BaseTest {

    static List<IRefactoring> refactorings;
    static ExtractOperationRefactoring extractOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\AxiosHttp.js"
                , getRootResourceDirectory() + "src2\\AxiosHttp.js");

        IRefactoring refactoring = refactorings.get(1);
        assertEquals(RefactoringType.EXTRACT_AND_MOVE_OPERATION, refactoring.getRefactoringType());
        extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
    }

    @Test
    void testRefactoringsCount() {
        assertEquals(2, refactorings.size());
    }

    @Test
    void testRefactoringType() {
        assertEquals(RefactoringType.EXTRACT_OPERATION, extractOperationRefactoring.getRefactoringType());
    }

    @Test
    void testFunctionNames() {
        assertEquals("settle", extractOperationRefactoring.getExtractedOperation().getName());
        assertEquals("AxiosHttp.js", extractOperationRefactoring.getSourceOperationAfterExtraction().getName());
        assertEquals("AxiosHttp.js", extractOperationRefactoring.getSourceOperationBeforeExtraction().getName());
    }

    @Test
    void testReplacementsCount() {
        assertEquals(1, extractOperationRefactoring.getReplacements().size());
    }

    @Test
    void testReplacements() {
        Iterator<Replacement> iterator = extractOperationRefactoring.getReplacements().iterator();
        Replacement replacement1 = iterator.next();
        assertEquals(ReplacementType.CONDITIONAL, replacement1.getType());
        assertEquals("(res.statusCode >= 200 && res.statusCode < 300 ? resolve : reject)(response);", replacement1.getBefore());
        assertEquals("(response.status >= 200 && response.status < 300 ? resolve : reject)(response);", replacement1.getAfter());
    }

    @Test
    void testUnmatchedStatementCount() {
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();
        assertEquals(1, mapper.getNonMappedLeavesT1().size());
        assertEquals(0, mapper.getNonMappedLeavesT2().size());
        assertEquals(0, mapper.getNonMappedInnerNodesT1().size());
        assertEquals(0, mapper.getNonMappedInnerNodesT2().size());
        // Parent mapper
        assertEquals(0, mapper.getParentMapper().getNonMappedLeavesT1().size());
        assertEquals(3, mapper.getParentMapper().getNonMappedLeavesT2().size());
        assertEquals(0, mapper.getParentMapper().getNonMappedInnerNodesT1().size());
        assertEquals(2, mapper.getParentMapper().getNonMappedInnerNodesT2().size());
    }

    @Test
    void testMatchedStatementCount() {
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();
        assertEquals(1, mapper.getMappings().size());
        assertEquals(11, mapper.getParentMapper().getMappings().size());
    }
}
