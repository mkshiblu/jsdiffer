package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionDeclarationTest {
    static IFunctionDeclaration functionDeclaration;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        sourceFile = parser.parseSource("function f1(p1, p2) { ; let   x,y=   1; } ", "file.js");
        functionDeclaration = sourceFile.getFunctionDeclarations().get(0);
    }

    @Test
    public void testName() {
        assertEquals("f1", functionDeclaration.getName());
    }

    @Test
    public void testQualifiedName() {
        assertEquals("f1", functionDeclaration.getQualifiedName());
    }

    @Test
    public void testParentContainerQualifiedName() {
        assertEquals("file.js", functionDeclaration.getParentContainerQualifiedName());
    }

    @Test
    public void testParameterCount() {
        assertEquals(2, functionDeclaration.getParameters().size());
    }

    @Test
    public void testParameterNames() {
        assertEquals("p1", functionDeclaration.getParameterNameList().get(0));
        assertEquals("p2", functionDeclaration.getParameterNameList().get(1));
    }

    @Test
    public void testStatementCount() {
        assertEquals(1, functionDeclaration.getStatements().size());
    }

    @Test
    public void testStatementParentText() {
        assertEquals("{", functionDeclaration.getStatements().get(0).getParent().getText());
    }
}
