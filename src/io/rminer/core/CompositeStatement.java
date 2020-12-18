package io.rminer.core;

import java.util.List;

public class CompositeStatement {
    private List<Statement> statementList;
    private List<Expression> expressionList;
    private List<VariableDeclaration> variableDeclarations;
    private List<ClassDeclaration> classDeclarations;
    private List<FunctionDeclaration> functionDeclarations;
}
