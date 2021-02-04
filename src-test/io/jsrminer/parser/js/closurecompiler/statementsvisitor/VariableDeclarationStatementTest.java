package io.jsrminer.parser.js.closurecompiler.statementsvisitor;

import io.jsrminer.parser.js.closurecompiler.StatementsVisitor;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclarationKind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VariableDeclarationStatementTest extends StatementsVisitorTest {

    static SingleStatement letAStatement;
    static SingleStatement vdWithLiteralInitializer;
    static SingleStatement vdWithAnonymousFunctionInitializer;
    static SingleStatement multipleVdsStatements;

    @BeforeAll
    public static void setup() {

        var letA = programTree.sourceElements.get(0);
        letAStatement = StatementsVisitor.variableStatementProcessor.visit(letA.asVariableStatement(), bodyBlock, container);
        vdWithLiteralInitializer = StatementsVisitor.variableStatementProcessor.visit(programTree.sourceElements.get(1).asVariableStatement(), bodyBlock, container);
        //   vdWithAnonymousFunctionInitializer = StatementsVisitor.variableStatementProcessor.process(programTree.sourceElements.get(2).asVariableStatement(), dummyBodyBlock, container);
        // multipleVdsStatements = StatementsVisitor.variableStatementProcessor.process(programTree.sourceElements.get(3).asVariableStatement(), dummyBodyBlock, container);
    }

    @Test
    public void letATextTest() {
        assertEquals("let a;", letAStatement.getText());
    }

    @Test
    public void letAVariablesCountTest() {
        assertEquals(1, letAStatement.getVariables().size());
    }

    @Test
    public void letAVariableNameTest() {
        assertEquals("a", letAStatement.getVariables().get(0));
    }

    @Test
    public void letAVariableDeclarationsCountTest() {
        assertEquals(1, letAStatement.getVariableDeclarations().size());
    }

    @Test
    public void letAVariableDeclarationsKindTest() {
        assertEquals(VariableDeclarationKind.LET, letAStatement.getVariableDeclarations().get(0).getKind());
    }

    @Test
    public void letAVariableDeclarationNameTest() {
        assertEquals("a", letAStatement.getVariableDeclarations().get(0).variableName);
    }

    // Test const const c = 34;

    @Test
    public void vdWithLiteralInitializerTextTest() {
        assertEquals("const c = 34;", vdWithLiteralInitializer.getText());
    }

    @Test
    public void vdWithLiteralInitializerLiteralCountTest() {
        assertEquals(1, vdWithLiteralInitializer.getNumberLiterals().size());
    }

    public void vdWithLiteralInitializerDeclarationsCountTest() {
        assertEquals(1, vdWithLiteralInitializer.getVariableDeclarations().size());
    }

    @Test
    public void vdWithLiteralInitializerInitializerTextTest() {
        assertNotNull(vdWithLiteralInitializer.getVariableDeclarations().get(0).getInitializer());
        assertEquals("34", vdWithLiteralInitializer.getVariableDeclarations().get(0).getInitializer().getText());
    }
}
