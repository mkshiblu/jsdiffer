package io.jsrminer.parser.js.babel;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.jsoniter.any.Any;

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
        this.nodeJs.getRuntime().executeVoidScript("function parse(script, asJson) { return parser.parse(script, asJson); }");
        this.nodeJs.getRuntime().executeVoidScript("function format(node, appendSemicolon = false) { return parser.format(node, appendSemicolon); }");
    }

    public Object executeFunction(final String name, final Object... args) {
        return this.nodeJs.getRuntime().executeJSFunction(name, args);
    }

    public String executeStringFunction(final String name, final Object... args) {
        String result;
        V8Array parameterArray = new V8Array(this.nodeJs.getRuntime());
        for (var object : args) {
            if (object instanceof Integer) {
                parameterArray.push((Integer) object);
            } else if (object instanceof Double) {
                parameterArray.push((Double) object);
            } else if (object instanceof Long) {
                parameterArray.push(((Long) object).doubleValue());
            } else if (object instanceof Float) {
                parameterArray.push((double) (Float) object);
            } else if (object instanceof Boolean) {
                parameterArray.push((Boolean) object);
            } else {
                parameterArray.push(object);
            }
        }
        try {
            result = this.nodeJs.getRuntime().executeStringFunction(name, parameterArray);
        } finally {
            parameterArray.release();
        }
        return result;
    }

    /**
     * Convert the following object to json in node js side. It's a helper function
     */
    public String toJson(Object object) {
        return this.nodeJs.getRuntime().executeJSFunction("toJson", object).toString();
    }

    /**
     * Format code in JS side
     */
    private String formatCode(V8Object v8Object) {
        return this.nodeJs.getRuntime().executeJSFunction("format", v8Object).toString();
    }

    private String formatCode(String source) {
        return executeStringFunction("format", source);
    }

    public BabelNode parse(String filename, String content) {
        V8Object ast = (V8Object) executeFunction("parse", content);
        //var result = executeStringFunction("parse", content, true);
        //var js = new JsonBabelNode(result, this::formatCode, filename);
        //return  js;

        return new V8BabelNode(ast, this::formatCode, filename);
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
