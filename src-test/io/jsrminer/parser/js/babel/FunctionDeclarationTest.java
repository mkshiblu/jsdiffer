package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclarationKind;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionDeclarationTest {

    static SingleStatement statement;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        sourceFile = parser.parseSource("function f1() { let   x,y=   1; } ", "file.js");
        statement = (SingleStatement) sourceFile.getStatements().get(0);
    }

    @Test
    public void testName() {
        assertEquals("let x, y = 1;", statement.getText());
    }
}
