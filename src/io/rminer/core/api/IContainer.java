package io.rminer.core.api;

/**
 * Rerpresents a code container such as FunctionDeclrations, Class Declarations or a File
 */
public interface IContainer {
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

    IComposite getBody();
}
