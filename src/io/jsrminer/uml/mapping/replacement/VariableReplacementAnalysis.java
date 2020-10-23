package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.mapping.LeafCodeFragmentMapping;

public class VariableReplacementAnalysis {
    public static boolean bothFragmentsUseVariable(VariableDeclaration vd, LeafCodeFragmentMapping mapping) {
        return ((SingleStatement) mapping.fragment1).getVariables().contains(vd.variableName) &&
                ((SingleStatement) mapping.fragment2).getVariables().contains(vd.variableName);
    }
}
