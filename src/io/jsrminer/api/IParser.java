package io.jsrminer.api;

import io.jsrminer.uml.UMLModel;

import java.util.List;
import java.util.Map;

public interface IParser {
    /**
     * Parses the source files and creates an uml model from it
     * @param fileContents Contains filepath and their contents map
     */
    UMLModel parse(Map<String, String> fileContents);
}
