package io.jsrminer.uml;

import io.jsrminer.sourcetree.FunctionDeclaration;

import java.util.HashMap;

/**
 * Abstracts the source code
 */
public class UMLModel {

    private HashMap<String, FunctionDeclaration[]> functionDeclarations;

    public UMLModelDiff diff(UMLModel umlModel) {
        // Todo function declarations
        return null;
    }

    public void setFunctionDeclarations(final HashMap<String, FunctionDeclaration[]> functionDeclarations) {
        this.functionDeclarations = functionDeclarations;
    }

    public HashMap<String, FunctionDeclaration[]> getFunctionDeclarations() {
        return functionDeclarations;
    }
}
