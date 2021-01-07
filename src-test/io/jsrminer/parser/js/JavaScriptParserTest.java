package io.jsrminer.parser.js;

import io.jsrminer.TestBase;
import io.rminer.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JavaScriptParserTest extends TestBase {
    protected static ISourceFile model;

    @BeforeAll
    public static void setup() throws IOException {
        JavaScriptParser parser = new JavaScriptParser();
        String sourceContent = Files.readString(Path.of(getRootResourceDirectory(), "vue.js"));
        model = parser.parseSource(sourceContent);
    }

    @Test
    public void topLevelfunctionDeclarationCount() {
        assertEquals(4, model.getFunctionDeclarations().size());
    }
}
