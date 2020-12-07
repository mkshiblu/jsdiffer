package io.jsrminer.parser;

import io.jsrminer.TestBase;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.uml.UMLModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class JsonCompositeParserTest extends TestBase {

    protected static UMLModel umlModel;

    @BeforeAll
    public static void setup() {
        
    }

    @AfterAll
    public static void finsh() {
        umlModel = null;
    }
}
