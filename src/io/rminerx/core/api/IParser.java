package io.rminerx.core.api;

import io.jsrminer.uml.UMLModel;

import java.util.Map;

public interface IParser {
    /**
     * Parses the source files and creates an uml model from it
     *
     * @param fileContents Contains filepath and their contents map
     */
    UMLModel parse(Map<String, String> fileContents);

    ISourceFile parseSource(String fileContent, String sourcePath);

    /**
     * Parse a json string containing the source code in composite
     * structure and construct an UML model from it
     */
    default UMLModel parse(String json) {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the source files and creates a composite json structure
     *
     * @param fileContents
     * @return
     */
    default String toJson(Map<String, String> fileContents) {
        throw new UnsupportedOperationException();
    }
}
