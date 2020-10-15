package io.jsrminer.sourcetree;

import java.util.ArrayList;
import java.util.List;

public class TryStatement extends BlockStatement {
    private List<BlockStatement> catchClauses = new ArrayList<>();
    private BlockStatement finallyClause;

    public TryStatement() {
        this.type = CodeElementType.TRY_STATEMENT;
    }

    public List<BlockStatement> getCatchClauses() {
        return catchClauses;
    }

    public void setFinallyClause(BlockStatement finallyClause) {
        this.finallyClause = finallyClause;
    }

    public BlockStatement getFinallyClause() {
        return finallyClause;
    }

    /*
    public List<VariableDeclaration> getVariableDeclarations() {
        List<VariableDeclaration> variableDeclarations = new ArrayList<();
        variableDeclarations.addAll(super.getVariableDeclarations());
        for (CompositeStatementObject catchClause : catchClauses) {
            variableDeclarations.addAll(catchClause.getVariableDeclarations());
        }
        return variableDeclarations;
    }*/
}
