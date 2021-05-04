package io.rminerx.core.entities;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.sourcetree.Statement;
import io.rminerx.core.api.IAnonymousClassDeclaration;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerBody {
    private final List<Statement> statements = new ArrayList<>();
    private final List<IFunctionDeclaration> functionDeclarations = new ArrayList<>();
    private final List<IClassDeclaration> classDeclarations = new ArrayList<>();
    private final List<IAnonymousClassDeclaration> anonymousClassDeclarations = new ArrayList<>();
    private final List<IAnonymousFunctionDeclaration> anonymousFunctionDeclarations = new ArrayList<>();

    public List<Statement> getStatements() {
        return statements;
    }

    public List<IFunctionDeclaration> getFunctionDeclarations() {
        return functionDeclarations;
    }

    public List<IClassDeclaration> getClassDeclarations() {
        return classDeclarations;
    }

    public List<IAnonymousClassDeclaration> getAnonymousClassDeclarations() {
        return anonymousClassDeclarations;
    }

    public List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations() {
        return anonymousFunctionDeclarations;
    }
}
