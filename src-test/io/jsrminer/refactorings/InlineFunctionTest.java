package io.jsrminer.refactorings;

import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.BaseTest;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.ObjectCreationReplacement;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InlineFunctionTest extends BaseTest {

    static List<IRefactoring> refactorings;
    static InlineOperationRefactoring inlineOperationRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(getRootResourceDirectory() + "ExtractOrInlineFunction\\src2"
                , getRootResourceDirectory() + "ExtractOrInlineFunction\\src1");

        IRefactoring refactoring = refactorings.get(0);
        inlineOperationRefactoring = (InlineOperationRefactoring) refactoring;
    }

    @Test
    void testRefactoringsCount() {
        assertNotNull(refactorings);
        assertEquals(8,refactorings.size());
    }

    @Test
    void testFunctionNames() {
        assertEquals(inlineOperationRefactoring.getInlinedOperation().getName(), "createAddress");
        assertEquals(inlineOperationRefactoring.getTargetOperationAfterInline().getName(), "createAddresses");
        assertEquals(inlineOperationRefactoring.getTargetOperationBeforeInline().getName(), "createAddresses");
        assertEquals("createAddress(\"127.0.0.1\", PORTS.incrementAndGet())", inlineOperationRefactoring.getInlinedOperationInvocations().get(0)
                .getText());
    }

    @Test
    void testRefactoringType() {
        IRefactoring refactoring = refactorings.get(0);
        assertEquals(RefactoringType.INLINE_OPERATION, refactoring.getRefactoringType());
    }

    @Test
    void testReplacements() {
        assertEquals(0, inlineOperationRefactoring.getReplacements().size());
    }

    @Test
    void testUnmatchedStatementCount() {
        FunctionBodyMapper mapper = inlineOperationRefactoring.getBodyMapper();
        assertEquals(1, mapper.getNonMappedLeavesT1().size());
        assertEquals(0, mapper.getNonMappedLeavesT2().size());
        assertEquals(0, mapper.getNonMappedInnerNodesT1().size());
        assertEquals(2, mapper.getNonMappedInnerNodesT2().size());
    }

    @Test
    void testMatchedStatementCount() {
        FunctionBodyMapper mapper = inlineOperationRefactoring.getBodyMapper();
        assertEquals(6, mapper.getMappings().size());
    }

    @Test
    void testMatchedStatementsMapping() {
        FunctionBodyMapper mapper = inlineOperationRefactoring.getBodyMapper();

        mapper.getExactMatches();
        Iterator<CodeFragmentMapping> iterator = mapper.getMappings().iterator();

        CodeFragmentMapping mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().startsWith("console")
                && mapping.fragment2.getText().startsWith("console")
                && mapping.getReplacements().size() == 0);

        mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().equals("return new Address(host, port);")
                && mapping.fragment2.getText().equals("addresses[i] = new Address(\"127.0.0.1\", PORTS.incrementAndGet());")
                && mapping.getReplacements().size() == 0);

        mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().equals("{")
                && mapping.fragment2.getText().equals("{")
                && mapping.getReplacements().size() == 0);


        mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().equals("try")
                && mapping.fragment2.getText().equals("try")
                && mapping.getReplacements().size() == 0);

        mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().equals("{")
                && mapping.fragment2.getText().equals("{")
                && mapping.getReplacements().size() == 0);


        mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().equals("catch(e)")
                && mapping.fragment2.getText().equals("catch(e)")
                && mapping.getReplacements().size() == 0);
    }

    @Test
    void testParentMapperMappings() {
        FunctionBodyMapper parentMapper = inlineOperationRefactoring.getBodyMapper().getParentMapper();

        assertEquals(2, parentMapper.getMappings().size());
        Iterator<CodeFragmentMapping> iterator = parentMapper.getMappings().iterator();
        CodeFragmentMapping mapping = iterator.next();

        assertTrue(mapping.fragment1.getText().equals("return addresses;")
                && mapping.fragment2.getText().equals("return addresses;")
                && mapping.getReplacements().size() == 0);

        mapping = iterator.next();
        assertTrue(mapping.fragment1.getText().equals("let addresses = [];")
                && mapping.fragment2.getText().equals("var addresses = new Array(count);")
                && mapping.getReplacements().size() == 2);


        // Test the replacements
        Iterator<Replacement> replacementIterator = mapping.getReplacements().iterator();
        Replacement replacement1 = replacementIterator.next();

        assertEquals("let", replacement1.getBefore());
        assertEquals("var", replacement1.getAfter());
        assertEquals(ReplacementType.KIND, replacement1.getType());

        Replacement replacement2 = replacementIterator.next();
        assertTrue(replacement2 instanceof ObjectCreationReplacement);

        assertEquals("[]", replacement2.getBefore());
        assertEquals("new Array(count)", replacement2.getAfter());
        assertEquals(ReplacementType.ARRAY_CONSTRUCTOR_REPLACED_WITH_ARRAY_CREATION, replacement2.getType());
    }
}
