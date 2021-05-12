package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclarationKind;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VariableDeclarationStatementTest {

    static SingleStatement statement;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        sourceFile = parser.parseSource("let   x,y=   1;", "file.js");
        statement = (SingleStatement) sourceFile.getStatements().get(0);
    }

    @Test
    public void testStatementText() {
        assertEquals("let x, y = 1;", statement.getText());
    }

    @Test
    public void testVariableDeclarationsCount() {
        assertEquals(2, statement.getVariableDeclarations().size());
    }

    @Test
    public void testStatementVariableNames() {
        assertEquals("x", statement.getVariables().get(0));
        assertEquals("y", statement.getVariables().get(1));
    }

    @Test
    public void testKind() {
        assertEquals(VariableDeclarationKind.LET, statement.getVariableDeclarations().get(0).getKind());
    }

    @Test
    public void testVariableDeclarationsVariableNames() {
        var vd1 = statement.getVariableDeclarations().get(0);
        assertEquals("x", vd1.getVariableName());
        var vd2 = statement.getVariableDeclarations().get(1);
        assertEquals("y", vd2.getVariableName());
    }

    @Test
    public void testStatementLiteralsCount() {
        assertEquals(1, statement.getNumberLiterals().size());
        var vd2 = statement.getVariableDeclarations().get(1);
        assertEquals(1, vd2.getInitializer().getNumberLiterals().size());
    }

    @Test
    public void testStatementLiteralsValue() {
        assertEquals("1", statement.getNumberLiterals().get(0));
    }

    @Test
    public void testInitializerExistence() {
        assertNull(statement.getVariableDeclarations().get(0).getInitializer());
        assertNotNull(statement.getVariableDeclarations().get(1).getInitializer());
    }

    @Test
    public void testVariableScope() {
        var scope1 = statement.getVariableDeclarations().get(0).getScope();
        var location1 = statement.getVariableDeclarations().get(0).getSourceLocation();
        var scope2 = statement.getVariableDeclarations().get(1).getScope();

        assertEquals(location1.start, scope1.start);
        assertEquals(location1.startLine, scope1.startLine);
        assertEquals(location1.startColumn, scope1.startColumn);
        assertEquals(sourceFile.getSourceLocation().endLine, scope1.endLine);
        assertEquals(sourceFile.getSourceLocation().endColumn, scope1.endColumn);
        assertEquals(sourceFile.getSourceLocation().end, scope1.end);
    }
}
