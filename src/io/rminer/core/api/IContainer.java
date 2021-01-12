package io.rminer.core.api;

import io.jsrminer.sourcetree.SourceLocation;

import java.util.List;

/**
 * Represents a code container such as FunctionDeclrations, Class Declarations or a File
 */
public interface IContainer {
    SourceLocation getSourceLocation();

    /**
     * Represents a container type such s File or Declaration
     **/
    enum ContainerType {
        /**
         * A File type of container.
         */
        File,
        /**
         * A declaration such as Class, or Function type container
         */
        Declaration
    }

    ContainerType getContainerType();

    List<ICodeFragment> getStatements();

    List<IFunctionDeclaration> getFunctionDeclarations();

    List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations();
}