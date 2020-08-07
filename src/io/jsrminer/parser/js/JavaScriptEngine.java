package io.jsrminer.parser.js;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8Object;
import io.jsrminer.io.FileUtil;

import java.io.File;

/**
 * Represents the JS engine or environment where JS scripts could be executed
 */
class JavaScriptEngine implements AutoCloseable {
    public static final String SCRIPTS_DIRECTORY_NAME = "scripts";
    private NodeJS nodeJs;
    private V8Object parser;

    public JavaScriptEngine() {
        this.nodeJs = NodeJS.createNodeJS();
        // Add a utility toJson function
        this.nodeJs.getRuntime().executeVoidScript("function toJson(object) {return JSON.stringify(object);}");
    }

    public void createParseFunction() {
        parser = this.nodeJs.require(new File(FileUtil.getResourcePath(SCRIPTS_DIRECTORY_NAME),
                "Parser.js"));
        this.nodeJs.getRuntime().add("parser", parser);
        this.nodeJs.getRuntime().executeVoidScript("function parse(script) { return parser.parse(script); }");
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
