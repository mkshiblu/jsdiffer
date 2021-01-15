package io.jsrminer.uml.mapping.replacement.replacers;

import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.TernaryOperatorExpression;
import io.jsrminer.uml.mapping.replacement.ReplacementFinder;
import io.jsrminer.uml.mapping.replacement.ReplacementInfo;
import io.jsrminer.uml.mapping.replacement.ReplacementType;

import java.util.LinkedHashSet;
import java.util.Set;

public class TernaryExpressionReplacer extends BaseReplacer {
    public static void apply(CodeFragment statement1, CodeFragment statement2, ReplacementInfo replacementInfo) {
        // region ternaryOpsExp
        if (statement1.getTernaryOperatorExpressions().isEmpty()
                && !statement2.getTernaryOperatorExpressions().isEmpty()) {
            if (!statement1.getNullLiterals().isEmpty()) {
                Set<String> nullLiterals1 = new LinkedHashSet<>();
                nullLiterals1.add("null");
                Set<String> ternaryExpressions2 = new LinkedHashSet<>();
                for (TernaryOperatorExpression ternary : statement2.getTernaryOperatorExpressions()) {
                    ternaryExpressions2.add(ternary.getText());
                }
                ReplacementFinder.findAndPerformBestReplacements(nullLiterals1, ternaryExpressions2, replacementInfo, ReplacementType.NULL_LITERAL_REPLACED_WITH_CONDITIONAL_EXPRESSION);
            }
        } else if (!statement1.getTernaryOperatorExpressions().isEmpty() && statement2.getTernaryOperatorExpressions().isEmpty()) {
            if (!statement2.getNullLiterals().isEmpty()) {
                Set<String> nullLiterals2 = new LinkedHashSet<String>();
                nullLiterals2.add("null");
                Set<String> ternaryExpressions1 = new LinkedHashSet<String>();
                for (TernaryOperatorExpression ternary : statement1.getTernaryOperatorExpressions()) {
                    ternaryExpressions1.add(ternary.getText());
                }
                ReplacementFinder.findAndPerformBestReplacements(ternaryExpressions1, nullLiterals2, replacementInfo, ReplacementType.NULL_LITERAL_REPLACED_WITH_CONDITIONAL_EXPRESSION);
            }
        }
    }
}
