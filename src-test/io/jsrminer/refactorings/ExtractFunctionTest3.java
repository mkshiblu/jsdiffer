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

public class ExtractFunctionTest3 extends BaseTest {

    static List<IRefactoring> refactorings;
    static ExtractOperationRefactoring extractOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenFiles(getRootResourceDirectory() + "src1\\ExtractFunction3.js"
                , getRootResourceDirectory() + "src2\\ExtractFunction3.js");

        IRefactoring refactoring = refactorings.get(0);
        assertEquals(RefactoringType.EXTRACT_OPERATION, refactoring.getRefactoringType());
        extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
    }

    @Test
    void testRefactoringsCount() {
        assertEquals(4, refactorings.size());
    }

    @Test
    void testRefactoringType() {
        assertEquals(RefactoringType.EXTRACT_OPERATION, extractOperationRefactoring.getRefactoringType());
    }

    @Test
    void testFunctionNames() {
        assertEquals("isKeyNotMatch", extractOperationRefactoring.getExtractedOperation().getName());
        assertEquals("checkKeyCodes", extractOperationRefactoring.getSourceOperationAfterExtraction().getName());
        assertEquals("checkKeyCodes", extractOperationRefactoring.getSourceOperationBeforeExtraction().getName());
    }

    @Test
    void testReplacementsCount() {
        assertEquals(2, extractOperationRefactoring.getReplacements().size());
    }

    @Test
    void testReplacements() {
        Iterator<Replacement> iterator = extractOperationRefactoring.getReplacements().iterator();
        Replacement replacement1 = iterator.next();
        Replacement replacement2 = iterator.next();
        assertEquals(ReplacementType.VARIABLE_NAME, replacement1.getType());
        assertEquals("eventKeyCode", replacement1.getBefore());
        assertEquals("actual", replacement1.getAfter());

        assertEquals(ReplacementType.VARIABLE_NAME, replacement2.getType());
        assertEquals("keyCodes", replacement2.getBefore());
        assertEquals("mappedKeyCode", replacement2.getAfter());
    }

    @Test
    void testUnmatchedStatementCount() {
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();
        assertEquals(0, mapper.getNonMappedLeavesT1().size());
        assertEquals(0, mapper.getNonMappedLeavesT2().size());
        assertEquals(0, mapper.getNonMappedInnerNodesT1().size());
        assertEquals(0, mapper.getNonMappedInnerNodesT2().size());
        // Parent mapper
        assertEquals(0, mapper.getParentMapper().getNonMappedLeavesT1().size());
        assertEquals(2, mapper.getParentMapper().getNonMappedLeavesT2().size());
        assertEquals(0, mapper.getParentMapper().getNonMappedInnerNodesT1().size());
        assertEquals(2, mapper.getParentMapper().getNonMappedInnerNodesT2().size());
    }

    @Test
    void testMatchedStatementCount() {
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();
        assertEquals(5, mapper.getMappings().size());
        assertEquals(6, mapper.getParentMapper().getMappings().size());
    }

    @Test
    void testMatchedStatementsMapping() {
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();

        mapper.getExactMatches();
        Iterator<CodeFragmentMapping> iterator = mapper.getMappings().iterator();

        CodeFragmentMapping mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().equals("return keyCodes.indexOf(eventKeyCode) === -1;")
                && mapping.fragment2.getText().equals("return expect.indexOf(actual) === -1;")
                && mapping.getReplacements().size() == 2);

//        mapping = iterator.next();
        //assertTrue("");
    }

    @Test
    void testParentMapperMappings() {
        FunctionBodyMapper parentMapper = extractOperationRefactoring.getBodyMapper().getParentMapper();

        Iterator<CodeFragmentMapping> iterator = parentMapper.getMappings().iterator();
        CodeFragmentMapping mapping = iterator.next();

        assertTrue(mapping.fragment1.getText().equals("return hyphenate(eventKeyName) !== key;")
                && mapping.fragment2.getText().equals("return hyphenate(eventKeyName) !== key;")
                && mapping.getReplacements().size() == 0);

//        mapping = iterator.next();
//        assertTrue(mapping.fragment1.getText().equals("var addresses = new Array(count);")
//                && mapping.fragment2.getText().equals("let addresses = [];")
//                && mapping.getReplacements().size() == 2);
//
//
//        Iterator<Replacement> replacementIterator = mapping.getReplacements().iterator();
//        Replacement replacement1 = replacementIterator.next();
//
//        assertEquals("var", replacement1.getBefore());
//        assertEquals("let", replacement1.getAfter());
//        assertEquals(ReplacementType.KIND, replacement1.getType());
//
//        Replacement replacement2 = replacementIterator.next();
//        assertTrue(replacement2 instanceof ObjectCreationReplacement);
//
//        assertEquals("new Array(count)", replacement2.getBefore());
//        assertEquals("[]", replacement2.getAfter());
//        assertEquals(ReplacementType.ARRAY_CONSTRUCTOR_REPLACED_WITH_ARRAY_CREATION, replacement2.getType());
    }
}
