package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.CodeFragment;

import java.util.Set;

public class CompositeReplacement extends Replacement {
    private Set<CodeFragment> additionallyMatchedStatements1;
    private Set<CodeFragment> additionallyMatchedStatements2;

    public CompositeReplacement(String before, String after,
                                Set<CodeFragment> additionallyMatchedStatements1, Set<CodeFragment> additionallyMatchedStatements2) {
        super(before, after, ReplacementType.COMPOSITE);
        this.additionallyMatchedStatements1 = additionallyMatchedStatements1;
        this.additionallyMatchedStatements2 = additionallyMatchedStatements2;
    }

    public Set<CodeFragment> getAdditionallyMatchedStatements1() {
        return additionallyMatchedStatements1;
    }

    public Set<CodeFragment> getAdditionallyMatchedStatements2() {
        return additionallyMatchedStatements2;
    }
}
