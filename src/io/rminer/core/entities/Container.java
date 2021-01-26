package io.rminer.core.entities;

import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.sourcetree.Statement;
import io.rminer.core.api.IAnonymousFunctionDeclaration;
import io.rminer.core.api.IContainer;
import io.rminer.core.api.IFunctionDeclaration;

import java.util.ArrayList;
import java.util.List;

public abstract class Container implements IContainer {
    protected final ContainerType containerType;
    protected final List<Statement> statements = new ArrayList<>();
    protected final List<IFunctionDeclaration> functionDeclarations = new ArrayList<>();
    protected SourceLocation sourceLocation;
    private List<IAnonymousFunctionDeclaration> anonymousFunctionDeclarations = new ArrayList<>();
    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    protected String qualifiedName;

    public Container(ContainerType containerType) {
        this.containerType = containerType;
    }

    @Override
    public ContainerType getContainerType() {
        return containerType;
    }

    @Override
    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public List<IFunctionDeclaration> getFunctionDeclarations() {
        return functionDeclarations;
    }

    public SourceLocation getSourceLocation(){
        return this.sourceLocation;
    }

    public List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations() {
        return anonymousFunctionDeclarations;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

}
