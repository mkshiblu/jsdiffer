package io.rminerx.core.api;

import io.jsrminer.sourcetree.Statement;

import java.util.List;
import java.util.Map;

/**
 * Represents a code container such as FunctionDeclrations, Class Declarations or a File
 */
public interface IContainer extends INode {
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

    List<Statement> getStatements();

    List<IFunctionDeclaration> getFunctionDeclarations();

    List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations();

    List<IClassDeclaration> getClassDeclarations();

    List<IAnonymousClassDeclaration> getAnonymousClassDeclarations();

    String getName();

    String getQualifiedName();

    /**
     * Returns the qualified name of the parent container
     * @return
     */
    String getParentContainerQualifiedName();
    /**
     * For depth 2 it will return all the functions including in statements, anonymous etc.
     */
    List<IFunctionDeclaration> getFunctionDeclarationsUpToDepth(int depth);

    /**
     * For depth 2 it will return all the functions including in anonymous etc.
     */
    Map<String, IFunctionDeclaration> getFunctionDeclarationsQualifiedNameMapUpToDepth(int depth);

    void registerFunctionDeclaration(IFunctionDeclaration functionDeclaration);
}
