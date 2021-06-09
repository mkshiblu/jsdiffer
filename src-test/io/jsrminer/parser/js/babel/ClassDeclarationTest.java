package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassDeclarationTest {
    static IClassDeclaration classDeclaration;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        sourceFile = parser.parseSource("class Circle extends Shape { static radius = 10; }", "file.js");
        classDeclaration = sourceFile.getClassDeclarations().get(0);
    }

    @Test
    public void testName() {
        assertEquals("Circle", classDeclaration.getName());
    }

    @Test
    public void testQualifiedName() {
        assertEquals("Circle", classDeclaration.getQualifiedName());
    }

    @Test
    public void testParentContainerQualifiedName() {
        assertEquals("file.js", classDeclaration.getParentContainerQualifiedName());
    }

//    @Test
//    public void testParameterCount() {
//        assertEquals(2, classDeclaration.getParameters().size());
//    }
//
//    @Test
//    public void testParameterNames() {
//        assertEquals("p1", functionDeclaration.getParameterNameList().get(0));
//        assertEquals("p2", functionDeclaration.getParameterNameList().get(1));
//    }
//
//    @Test
//    public void testStatementCount() {
//        assertEquals(1, functionDeclaration.getStatements().size());
//    }
//
//    @Test
//    public void testStatementParentText() {
//        assertEquals("{", functionDeclaration.getStatements().get(0).getParent().getText());
//    }
}
