package io.jsrminer.parser;

import io.jsrminer.BaseTest;
import io.jsrminer.parser.js.babel.BabelParser;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.Statement;
import io.rminerx.core.api.IParser;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SourceFileModelTest extends BaseTest {
    protected static FunctionDeclaration[] functions;
    protected static List<Statement> statements;
    protected static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() throws IOException {
        IParser parser = new BabelParser();
        String sourceContent = Files.readString(Path.of(getRootResourceDirectory(), "source_model.js"));
        sourceFile = parser.parseSource(sourceContent, "source_model.js");
        functions = sourceFile.getFunctionDeclarations().toArray(FunctionDeclaration[]::new);
        statements = sourceFile.getStatements();
    }

    @Test
    public void testElementsCount() {
        assertEquals(2, functions.length);
        assertEquals(2, sourceFile.getAnonymousFunctionDeclarations().size());
        assertEquals(5, statements.size());
    }

    @AfterAll
    public static void finish() {
        functions = null;
    }
}
