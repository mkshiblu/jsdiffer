package io.rminer.core.api;

import io.jsrminer.sourcetree.FunctionDeclaration;

public interface ISourceFile {
    /**
     * Returns the list of top level function declared in this file
     *
     * @return
     */
    FunctionDeclaration[] getFunctionDeclarations();
}
