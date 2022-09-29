package io.jsrminer.parser.js;

import io.jsrminer.parser.js.babel.BabelParser;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamedAnonymousFunctionTest {
    static IAnonymousFunctionDeclaration anonymousAsFunction;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() {
        var parser = new BabelParser();
        sourceFile = parser.parseSource("function w(){} x.y = function a1(){};", "file.js");
        anonymousAsFunction = sourceFile.getAnonymousFunctionDeclarations().get(0);
    }

    @Test
    public void testName() {
        assertEquals("a1", anonymousAsFunction.getOptionalName());
    }
}