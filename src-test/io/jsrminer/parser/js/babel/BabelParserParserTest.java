package io.jsrminer.parser.js.babel;

import io.jsrminer.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BabelParserParserTest extends BaseTest {

    @Test
    public void testParseCode() {
        var parser = new BabelParser();
        var result = parser.parseSource("let x,y = 1;", "file.js");
        assertNotNull(result);
    }

    @Test
    public void testCallBack() {

    }
}
