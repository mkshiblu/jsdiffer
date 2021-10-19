package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.SingleStatement;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemberExpressionStatementTest {
    static SingleStatement statement;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        sourceFile = parser.parseSource("function f1 (p1, p2) {\n" +
                "    x.y =  !z }", "file.js");
        statement = (SingleStatement) sourceFile.getFunctionDeclarations().get(0).getStatements().get(0);
    }

    @Test
    public void testStatementText() {
        assertEquals("x.y = !z;", statement.getText());
    }

    @Test
    public void testStatementType() {
        assertEquals(CodeElementType.EXPRESSION_STATEMENT, statement.getCodeElementType());
    }


    @Test
    public void testPrefixExpressionCount() {
        assertEquals(1, statement.getPrefixExpressions().size());
    }

    @Test
    public void testPrefixExpressionText() {
        assertEquals("!z", statement.getPrefixExpressions().get(0));
    }

    @Test
    public void testPostfixExpressionCount() {
        assertEquals(0, statement.getPostfixExpressions().size());
    }

    @Test
    public void testInfixOperatorCount() {
        assertEquals(0, statement.getInfixOperators().size());
    }

//    @Test
//    public void testInfixExpressionText() {
//        assertEquals("x.y = !z", statement.getInfixExpressions().get(0));
//    }

    @Test
    public void testVariableCount() {
        assertEquals(3, statement.getVariables().size());
    }
}
