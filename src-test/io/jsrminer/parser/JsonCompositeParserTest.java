package io.jsrminer.parser;

import io.jsrminer.TestBase;
import io.jsrminer.parser.js.JavaScriptParser;
import io.jsrminer.sourcetree.FunctionDeclaration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonCompositeParserTest extends TestBase {

    protected static FunctionDeclaration[] functions;

    @BeforeAll
    public static void setup() throws IOException {
        JavaScriptParser parser = new JavaScriptParser();
        String sourceContent = Files.readString(Path.of(getRootResourceDirectory() + "real-projects", "jquery_qunit.js"));
        functions = parser.parseSource(sourceContent).getFunctionDeclarations();
    }

//    @Test
//    public void testElementsCount(){
//        Function1 =
//    }

    @AfterAll
    public static void finsh() {
        functions = null;
    }
}
