package io.jsrminer.uml;

import io.rminer.core.api.IParser;
import io.jsrminer.parser.js.JavaScriptParser;

import java.util.Map;

public class UMLModelFactory {
//    public static UMLModel createUMLModel(Map<String, String> fileContents, Set<String> repositoryDirectories) {
//        JavaScriptParser parser = new JavaScriptParser();
//        return parser.parse(fileContents/*, repositoryDirectories*/);
//    }

    public static UMLModel createUMLModel(Map<String, String> fileContents) {
        IParser parser = new JavaScriptParser();
        return parser.parse(fileContents);
    }
}
