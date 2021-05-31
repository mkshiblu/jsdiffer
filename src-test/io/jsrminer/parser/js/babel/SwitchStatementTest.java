package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwitchStatementTest {
    static BlockStatement statement;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        var sourceFile = parser.parseSource("switch (x) { case 5:  case 2: break;" +
                " default: f2(); break; }", "file.js");
        statement = (BlockStatement) sourceFile.getStatements().get(0);
    }

    @Test
    public void testStatementType() {
        assertEquals(CodeElementType.SWITCH_STATEMENT, statement.getCodeElementType());
    }

    @Test
    public void testStatementCount() {
        assertEquals(6, statement.getStatements().size());
    }

    @Test
    public void testStatementText() {
        assertEquals("case 5:", statement.getStatements().get(0).getText());
        assertEquals("case 2:", statement.getStatements().get(1).getText());
        assertEquals("break;", statement.getStatements().get(2).getText());
        assertEquals("default:", statement.getStatements().get(3).getText());
        assertEquals("f2();", statement.getStatements().get(4).getText());
        assertEquals("break;", statement.getStatements().get(5).getText());
    }
}
