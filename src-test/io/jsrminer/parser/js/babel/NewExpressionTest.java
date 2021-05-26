package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.SingleStatement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewExpressionTest {

    static SingleStatement leaf;
    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        var sourceFile = parser.parseSource("var x = new function () { let counter = 0; this.incrementAndGet = function () { return ++counter; }; }()", "file.js");
        leaf = (SingleStatement) sourceFile.getStatements().get(0);
    }

    @Test
    public void testObjectCreationCount() {
        assertEquals(1, leaf.getCreationMap().size());
    }
//
//    @Test
//    public void testQualifiedName() {
//        assertEquals("a.1", anonymous.getQualifiedName());
//        assertEquals("a.1.1", innerAnonymous.getQualifiedName());
//    }
//
//    @Test
//    public void testParentContainerQualifiedName() {
//        assertEquals("a", anonymous.getParentContainerQualifiedName());
//        assertEquals("a.1", innerAnonymous.getParentContainerQualifiedName());
//    }
//
//    @Test
//    public void testParameterCount() {
//        assertEquals(2, anonymous.getParameters().size());
//    }
//
//    @Test
//    public void testParameterNames() {
//        assertEquals("p1", anonymous.getParameterNameList().get(0));
//        assertEquals("p2", anonymous.getParameterNameList().get(1));
//    }
//
//    @Test
//    public void testStatementCount() {
//        assertEquals(1, anonymous.getStatements().size());
//    }
//
//    @Test
//    public void testStatementParentText() {
//        assertEquals("{", anonymous.getStatements().get(0).getParent().getText());
//    }
}
