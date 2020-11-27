package io.jsrminer.parser.js;

import io.jsrminer.TestBase;
import io.jsrminer.sourcetree.SourceFileModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JavaScriptParserTest extends TestBase {
    protected static SourceFileModel model;

    @BeforeAll
    public static void setup() throws IOException {
        JavaScriptParser parser = new JavaScriptParser();
        String sourceContent = Files.readString(Path.of(getRootResourceDirectory() + "real-sources", "jquery_qunit.js"));
        model = parser.parseSource(sourceContent);
    }

    @Test
    public void topLevelfunctionDeclarationCount() {
        assertEquals(1, model.getFunctionDeclarations().length);
    }
}
