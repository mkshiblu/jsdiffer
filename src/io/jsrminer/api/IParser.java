package io.jsrminer.api;

import io.jsrminer.uml.UMLModel;

import java.util.List;

public interface IParser {
    UMLModel parse(List<String> sourceFiles);
}
