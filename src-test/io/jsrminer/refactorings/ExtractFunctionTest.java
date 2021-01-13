package io.jsrminer.refactorings;

import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.TestBase;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.ObjectCreationReplacement;
import io.jsrminer.uml.mapping.replacement.Replacement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExtractFunctionTest extends TestBase {

    static List<IRefactoring> refactorings;
    static ExtractOperationRefactoring extractOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "ExtractOrInlineFunction\\src1"
                , getRootResourceDirectory() + "ExtractOrInlineFunction\\src2");

        IRefactoring refactoring = refactorings.get(0);
        assertEquals(RefactoringType.EXTRACT_OPERATION, refactoring.getRefactoringType());
        extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
    }

    @Test
    void testFunctionNames() {
        assertEquals(extractOperationRefactoring.getExtractedOperation().getName(), "createAddress");
        assertEquals(extractOperationRefactoring.getSourceOperationAfterExtraction().getName(), "createAddresses");
        assertEquals(extractOperationRefactoring.getSourceOperationBeforeExtraction().getName(), "createAddresses");
    }

    @Test
    void testRefactoringsCount() {
        assertNotNull(refactorings);
        assertEquals(6, refactorings.size());
    }

    @Test
    void testRefactoringType() {
        IRefactoring refactoring = refactorings.get(0);
        assertEquals(RefactoringType.EXTRACT_OPERATION, refactoring.getRefactoringType());
    }

    @Test
    void testReplacements() {
        assertEquals(0, extractOperationRefactoring.getReplacements().size());
    }

    @Test
    void testUnmatchedStatementCount() {
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();
        assertEquals(1, mapper.getNonMappedLeavesT1().size());
        assertEquals(1, mapper.getNonMappedLeavesT2().size());
        assertEquals(2, mapper.getNonMappedInnerNodesT1().size());
        assertEquals(0, mapper.getNonMappedInnerNodesT2().size());
    }

    @Test
    void testMatchedStatementCount() {
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();
        assertEquals(6, mapper.getMappings().size());
    }

    @Test
    void testMatchedStatementsMapping() {
        FunctionBodyMapper mapper = extractOperationRefactoring.getBodyMapper();

        mapper.getExactMatches();
        Iterator<CodeFragmentMapping> iterator = mapper.getMappings().iterator();

        CodeFragmentMapping mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().startsWith("console")
                && mapping.fragment2.getText().startsWith("console")
                && mapping.getReplacements().size() == 0);

        mapping = iterator.next();
        //assertTrue("");
    }

    @Test
    void testParentMapperMappings() {
        FunctionBodyMapper parentMapper = extractOperationRefactoring.getBodyMapper().getParentMapper();

        assertEquals(2, parentMapper.getMappings().size());
        Iterator<CodeFragmentMapping> iterator = parentMapper.getMappings().iterator();
        CodeFragmentMapping mapping = iterator.next();

        assertTrue(mapping.fragment1.getText().equals("return addresses;")
                && mapping.fragment2.getText().equals("return addresses;")
                && mapping.getReplacements().size() == 0);

        mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().equals("var addresses = new Array(count);")
                && mapping.fragment2.getText().equals("let addresses = [];")
                && mapping.getReplacements().size() == 2);


        Iterator<Replacement> replacementIterator = mapping.getReplacements().iterator();
        Replacement replacement1 = replacementIterator.next();

        assertEquals("var", replacement1.getBefore());
        assertEquals("let", replacement1.getAfter());
        assertEquals(Replacement.ReplacementType.KIND, replacement1.getType());

        Replacement replacement2 = replacementIterator.next();
        assertTrue(replacement2 instanceof ObjectCreationReplacement);

        assertEquals("new Array(count)", replacement2.getBefore());
        assertEquals("[]", replacement2.getAfter());
        assertEquals(Replacement.ReplacementType.ARRAY_CONSTRUCTOR_REPLACED_WITH_ARRAY_CREATION, replacement2.getType());
    }
}
