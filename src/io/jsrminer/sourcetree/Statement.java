package io.jsrminer.sourcetree;

import io.rminer.core.api.ICodeFragment;

import java.util.List;

public abstract class Statement extends CodeFragment implements ICodeFragment {

    protected BlockStatement parent;

    public Statement() {

    }

    public BlockStatement getParent() {
        return parent;
    }

    public void setParent(BlockStatement parent) {
        this.parent = parent;
    }

    public abstract int statementCount();


    @Override
    public VariableDeclaration findVariableDeclarationIncludingParent(String variableName) {
        VariableDeclaration variableDeclaration = this.getVariableDeclaration(variableName);
        if (variableDeclaration != null) {
            return variableDeclaration;
        } else if (parent != null) {
            return parent.findVariableDeclarationIncludingParent(variableName);
        }
        return null;
    }

    @Override
    public VariableDeclaration getVariableDeclaration(String variableName) {
        List<VariableDeclaration> variableDeclarations = getVariableDeclarations();
        for (VariableDeclaration declaration : variableDeclarations) {
            if (declaration.variableName.equals(variableName)) {
                return declaration;
            }
        }
        return null;
    }
}
