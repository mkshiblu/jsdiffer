package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionExpressionTest {
    static IAnonymousFunctionDeclaration anonymous;
    static IAnonymousFunctionDeclaration innerAnonymous;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        sourceFile = parser.parseSource(" function a() { var m = function (p1, p2) {  function f2(){}  \n" +
                "  var t, z = function () { };\n" +
                "};}", "file.js");
        anonymous = sourceFile.getFunctionDeclarations().get(0).getAnonymousFunctionDeclarations().get(0);
        innerAnonymous = anonymous.getAnonymousFunctionDeclarations().get(0);
    }

    @Test
    public void testName() {
        assertEquals("1", anonymous.getName());
        assertEquals("1", innerAnonymous.getName());
    }

    @Test
    public void testQualifiedName() {
        assertEquals("a.1", anonymous.getQualifiedName());
        assertEquals("a.1.1", innerAnonymous.getQualifiedName());
    }

    @Test
    public void testParentContainerQualifiedName() {
        assertEquals("a", anonymous.getParentContainerQualifiedName());
        assertEquals("a.1", innerAnonymous.getParentContainerQualifiedName());
    }

    @Test
    public void testParameterCount() {
        assertEquals(2, anonymous.getParameters().size());
    }

    @Test
    public void testParameterNames() {
        assertEquals("p1", anonymous.getParameterNameList().get(0));
        assertEquals("p2", anonymous.getParameterNameList().get(1));
    }

    @Test
    public void testStatementCount() {
        assertEquals(1, anonymous.getStatements().size());
    }

    @Test
    public void testStatementParentText() {
        assertEquals("{", anonymous.getStatements().get(0).getParent().getText());
    }
}
