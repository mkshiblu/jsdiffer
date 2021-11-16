package io.jsrminer.parser.js;

import io.jsrminer.parser.js.babel.BabelParser;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionExpressionAsDeclarationTest {
    static IFunctionDeclaration anonymousAsFunction;
    static IAnonymousFunctionDeclaration innerAnonymous;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        sourceFile = parser.parseSource(" function a() { var m = function (p1, p2) {  function f2(){}  \n" +
                "  var t, z = function () { };\n" +
                "};}", "file.js");
        anonymousAsFunction = sourceFile.getFunctionDeclarations().get(0).getFunctionDeclarations().get(0);
        innerAnonymous = anonymousAsFunction.getAnonymousFunctionDeclarations().get(0);
    }

    @Test
    public void testName() {
        assertEquals("m", anonymousAsFunction.getName());
        assertEquals("1", innerAnonymous.getName());
    }

    @Test
    public void testQualifiedName() {
        assertEquals("a.m", anonymousAsFunction.getQualifiedName());
        assertEquals("a.m.1", innerAnonymous.getQualifiedName());
    }

    @Test
    public void testParentContainerQualifiedName() {
        assertEquals("a", anonymousAsFunction.getParentContainerQualifiedName());
        assertEquals("a.m", innerAnonymous.getParentContainerQualifiedName());
    }

    @Test
    public void testParameterCount() {
        assertEquals(2, anonymousAsFunction.getParameters().size());
    }

    @Test
    public void testParameterNames() {
        assertEquals("p1", anonymousAsFunction.getParameterNameList().get(0));
        assertEquals("p2", anonymousAsFunction.getParameterNameList().get(1));
    }

    @Test
    public void testStatementCount() {
        assertEquals(1, anonymousAsFunction.getStatements().size());
    }

    @Test
    public void testStatementParentText() {
        assertEquals("{", anonymousAsFunction.getStatements().get(0).getParent().getText());
    }
}
