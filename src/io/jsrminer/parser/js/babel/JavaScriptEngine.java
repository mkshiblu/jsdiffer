package io.jsrminer.parser.js.babel;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8Object;

import java.io.File;

/**
 * Represents the JS engine or environment where JS scripts could be executed
 */
class JavaScriptEngine implements AutoCloseable {
    public static final String PARSE_SCRIPT_FILE = "src-js/src-compiled.js";
    private NodeJS nodeJs;
    private V8Object parser;

    public JavaScriptEngine() {
        this.nodeJs = NodeJS.createNodeJS();
        // Add a utility toJson function
        this.nodeJs.getRuntime().executeVoidScript("function toJson(object) { return JSON.stringify(object);}");
    }

    public void createParseFunction() {
        parser = this.nodeJs.require(new File(PARSE_SCRIPT_FILE));
        this.nodeJs.getRuntime().add("parser", parser);
        this.nodeJs.getRuntime().executeVoidScript("function parse(script, asJson) { return parser.parse(script, asJson); }");
    }

    public Object executeFunction(final String name, final Object... args) {
        return this.nodeJs.getRuntime().executeJSFunction(name, args);
    }

    /**
     * Convert the following object to json in node js side. It's a helper function
     */
    public String toJson(Object object) {
        return this.nodeJs.getRuntime().executeJSFunction("toJson", object).toString();
    }

    @Override
    public void close() throws Exception {
        if (parser != null) {
            parser.release();
        }

        if (nodeJs != null) {
            nodeJs.release();
        }
    }
}
