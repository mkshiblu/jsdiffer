package io.rminer.core.entities;

import io.rminer.core.api.IComposite;
import io.rminer.core.api.IVariableDeclaration;

import java.util.List;

public class CompositeFragment extends Fragment implements IComposite {
    private List<Statement> statementList;
    private List<ExpressionFragment> expressionList;
    private List<IVariableDeclaration> variableDeclarations;
    private List<ClassDeclaration> classDeclarations;
    private List<FunctionDeclaration> functionDeclarations;
}
