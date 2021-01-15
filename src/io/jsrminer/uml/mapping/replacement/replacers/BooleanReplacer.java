package io.jsrminer.uml.mapping.replacement.replacers;

import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.JsConfig;
import io.jsrminer.uml.diff.CodeFragmentDiff;
import io.jsrminer.uml.mapping.replacement.ReplacementFinder;
import io.jsrminer.uml.mapping.replacement.ReplacementInfo;
import io.jsrminer.uml.mapping.replacement.ReplacementType;

public class BooleanReplacer extends BaseReplacer {
    public static void apply(CodeFragment statement1, CodeFragment statement2, ReplacementInfo replacementInfo
            , CodeFragmentDiff diff) {
        if (!statement1.getText().endsWith(JsConfig.TEXT_ASSIGNING_TRUE)
                && !statement1.getText().endsWith(JsConfig.TEXT_ASSIGNING_FALSE)) {
            ReplacementFinder.findAndPerformBestReplacements(diff.booleanLiterals1, diff.variables2, replacementInfo, ReplacementType.BOOLEAN_REPLACED_WITH_VARIABLE);
        }
        if (!statement2.getText().endsWith("= true" + JsConfig.STATEMENT_TERMINATOR_CHAR) && !statement2.getText().endsWith("= false" + JsConfig.STATEMENT_TERMINATOR_CHAR)) {
            ReplacementFinder.findAndPerformBestReplacements(diff.arguments1, diff.booleanLiterals2, replacementInfo, ReplacementType.BOOLEAN_REPLACED_WITH_ARGUMENT);
        }
    }
}
