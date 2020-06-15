package io.jsrminer.uml;

import io.jsrminer.api.IParser;
import io.jsrminer.parser.JavaScriptParser;

import java.util.Map;
import java.util.Set;

public class UMLModelFactory {
    public static UMLModel createUMLModel(Map<String, String> fileContents, Set<String> repositoryDirectories) {
        JavaScriptParser parser = new JavaScriptParser();
        return parser.parse(fileContents/*, repositoryDirectories*/);
    }
}
