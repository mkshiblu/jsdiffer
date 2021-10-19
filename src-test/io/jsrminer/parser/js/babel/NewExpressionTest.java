package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.SingleStatement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NewExpressionTest {

    static SingleStatement leaf;
    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        var sourceFile = parser.parseSource("var x = new Person(a1);", "file.js");
        leaf = (SingleStatement) sourceFile.getStatements().get(0);
    }

    @Test
    public void testObjectCreationCount() {
        assertEquals(1, leaf.getCreationMap().size());
    }

    @Test
    public void testObjectCreationText() {
        var objectCreation  = leaf.getCreationMap().values().stream().findFirst().get();
        assertTrue("new Person(a1)".equals(objectCreation.get(0).getText()));
    }

    @Test
    public void testStatementVariablesCount() {
        assertEquals(2, leaf.getVariables().size());
    }

    @Test
    public void testArgumentsCount() {
        var objectCreation  = leaf.getCreationMap().values().stream().findFirst().get().get(0);
        assertEquals(1, objectCreation.getArguments().size());
    }

    @Test
    public void testArgumentText() {
        var objectCreation  = leaf.getCreationMap().values().stream().findFirst().get().get(0);
        assertEquals("a1", objectCreation.getArguments().get(0));
    }

//    @Test
//    public void testStatementTypeCount() {
//        assertEquals(1, leaf.getVariables().size());
//    }
}
