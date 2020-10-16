package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.Statement;
import io.jsrminer.uml.mapping.replacement.Replacement;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class StatementMapping {
    public final Statement statement1;
    public final Statement statement2;

    private Set<Replacement> replacements = new LinkedHashSet<>();

    public StatementMapping(Statement statement1, Statement statement2) {
        this.statement1 = statement1;
        this.statement2 = statement2;
    }

    public abstract boolean isExactMatch();

    @Override
    public String toString() {
        return statement1.toString() + statement2.toString();
    }

    public void addReplacements(Set<Replacement> replacements) {
        this.replacements.addAll(replacements);
    }

    public Set<Replacement> getReplacements() {
        return replacements;
    }
}
