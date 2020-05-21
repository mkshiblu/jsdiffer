package io.jsrminer.parser;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents the JS engine or environment where JS scripts could be executed
 */
class JavaScriptEngine {
    public static final String NODE_MODULES_DIRECTORY_NAME = "node_modules";

    private NodeJS nodeJs;
    private File nodeModules;
    private V8Object babelV8Object;

    public void createNodeJsEnvironment() {
        this.nodeJs = NodeJS.createNodeJS();
        URL nodeModulesUrl = this.getClass().getClassLoader().getResource(NODE_MODULES_DIRECTORY_NAME);

        if (nodeModulesUrl.toString().startsWith("jar:")) {
            String tempFolder = System.getProperty("java.io.tmpdir");
            nodeModules = new File(tempFolder, "rminer_" + NODE_MODULES_DIRECTORY_NAME);
            createNodeJSLibFiles("@babel/parser/package.json",
                    "@babel/parser/lib/index.js",
                    "@babel/parser/bin/babel-parser.js");
        } else {
            nodeModules = new File(nodeModulesUrl.getFile());
        }
    }

    /**
     * Create a function parse(script) in the environment which calls the underlying babelParser.parse function
     * Also creates a toJson(object) function to format an object as json
     */
    public void addBabelParser() {
        this.babelV8Object = this.nodeJs.require(new File(nodeModules, "@babel/parser"));
        this.nodeJs.getRuntime().add("babelParser", this.babelV8Object);
        String plugins = "['jsx', 'objectRestSpread', 'exportDefaultFrom', 'exportNamespaceFrom', 'classProperties', 'flow', 'dynamicImport', 'decorators', 'optionalCatchBinding']";
        this.nodeJs.getRuntime().executeVoidScript("function parse(script) {return babelParser.parse(script, {ranges: true, tokens: true, sourceType: 'unambiguous', allowImportExportEverywhere: true, allowReturnOutsideFunction: true, plugins: " + plugins + " });}");
        this.nodeJs.getRuntime().executeVoidScript("function toJson(object) {return JSON.stringify(object);}");
    }

    public Object executeFunction(final String name, final Object... args) {
        return this.nodeJs.getRuntime().executeJSFunction(name, args);
    }

    private void createNodeJSLibFiles(String... paths) {
        ClassLoader cl = this.getClass().getClassLoader();
        for (String path : paths) {
            Path destPath = nodeModules.toPath().resolve(path);
            if (!Files.exists(destPath)) {
                try (InputStream is = cl.getResourceAsStream(NODE_MODULES_DIRECTORY_NAME + "/" + path)) {
                    Files.createDirectories(destPath.getParent());
                    Files.copy(is, destPath);
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Could not copy %s to %s", path, nodeModules.toString()), e);
                }
            }
        }
    }
}
