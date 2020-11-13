package io.jsrminer.uml;

import io.jsrminer.parser.js.JavaScriptParser;

import java.util.Map;
import java.util.Set;

public class UMLModelFactory {
    public static UMLModel createUMLModel(Map<String, String> fileContents, Set<String> repositoryDirectories) {
        JavaScriptParser parser = new JavaScriptParser();
        return parser.parse(fileContents/*, repositoryDirectories*/);
    }

    public static UMLModel createUMLModel(Map<String, String> fileContents) {
        JavaScriptParser parser = new JavaScriptParser();
        return parser.parse(fileContents);
    }
}
