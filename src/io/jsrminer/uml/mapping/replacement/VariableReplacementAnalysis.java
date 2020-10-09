package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.mapping.LeafStatementMapping;

public class VariableReplacementAnalysis {
    public static boolean bothFragmentsUseVariable(VariableDeclaration vd, LeafStatementMapping mapping) {
        return ((SingleStatement) mapping.statement1).getVariables().contains(vd.variableName) &&
                ((SingleStatement) mapping.statement2).getVariables().contains(vd.variableName);
    }
}
