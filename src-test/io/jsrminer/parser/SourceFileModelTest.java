package io.jsrminer.parser;

import io.jsrminer.TestBase;
import io.rminer.core.api.IParser;
import io.jsrminer.parser.js.JavaScriptParser;
import io.jsrminer.sourcetree.FunctionDeclaration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SourceFileModelTest extends TestBase {
    protected static FunctionDeclaration[] functions;
    protected static FunctionDeclaration checkKeyCodesFunction;

    @BeforeAll
    public static void setup() throws IOException {
        IParser parser = new JavaScriptParser();
        String sourceContent = Files.readString(Path.of(getRootResourceDirectory() , "vue.js"));
        functions = parser.parseSource(sourceContent).getFunctionDeclarations();
        checkKeyCodesFunction = functions[functions.length];
    }

    @Test
    public void testElementsCount() {
        assertEquals(4, functions.length);
    }

    @AfterAll
    public static void finish() {
        functions = null;
    }
}
