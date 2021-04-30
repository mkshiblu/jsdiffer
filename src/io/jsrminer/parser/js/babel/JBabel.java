package io.jsrminer.parser.js.babel;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8Object;

import java.io.File;

/**
 * Represents the JS engine or environment where JS scripts could be executed
 */
class JBabel implements AutoCloseable {
    public static final String PARSE_SCRIPT_FILE = "src-js/babel_parser.js";
    private NodeJS nodeJs;
    private V8Object parser;

    public JBabel() {
        this.nodeJs = NodeJS.createNodeJS();
        // Add a utility toJson function
        this.nodeJs.getRuntime().executeVoidScript("function toJson(object) { return JSON.stringify(object);}");
        init();
    }

    private void init() {
        parser = this.nodeJs.require(new File(PARSE_SCRIPT_FILE));
        this.nodeJs.getRuntime().add("parser", parser);
        this.nodeJs.getRuntime().executeVoidScript("function parse(script, asJson) { return parser.parse(script); }");
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

    public JV8 parse(String filename, String content) {
        V8Object ast = (V8Object) executeFunction("parse", content);
        return new JV8(ast, this::toJson);
    }

    @Override
    public void close() {
        if (parser != null) {
            parser.release();
        }

        if (nodeJs != null) {
            nodeJs.release();
        }
    }
}
