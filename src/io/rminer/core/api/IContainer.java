package io.rminer.core.api;

/**
 * Rerpresents a code container such as FunctionDeclrations, Class Declarations or a File
 */
public interface IContainer {
    IComposite getBody();
}
