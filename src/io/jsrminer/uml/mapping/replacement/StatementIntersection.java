package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.SingleStatement;

import java.util.LinkedHashSet;
import java.util.Set;

public class StatementIntersection {
    public final SingleStatement statement1;
    public final SingleStatement statement2;
    public final Set<String> unmatchedStringLiterals1 = new LinkedHashSet<>();
    public final Set<String> unmatchedStringLiterals2 = new LinkedHashSet<>();
    public final Set<String> unmatchedNumberLiterals1 = new LinkedHashSet<>();
    public final Set<String> unmatchedNumberLiterals2 = new LinkedHashSet<>();
    public final Set<String> unmatchedArguments1 = new LinkedHashSet<>();
    public final Set<String> unmatchedArguments2 = new LinkedHashSet<>();
    public final Set<String> unmatchedVariables1 = new LinkedHashSet<>();
    public final Set<String> unmatchedVariables2 = new LinkedHashSet<>();

    public StatementIntersection(SingleStatement statement1, SingleStatement statement2) {
        this.statement1 = statement1;
        this.statement2 = statement2;
        //intersectVariables();
        intersectStringLiterals();
        intersectNumberLiterals();
        intersectArgumentIdentifiers();
    }

//    private void intersectVariables() {
//        unmatchedVariables1 = new LinkedHashSet<>(statement1.getVariables());
//        unmatchedVariables1 = new LinkedHashSet<>(statement2.getVariables());
//        ReplacementUtil.removeCommonElements(unmatchedVariables1, unmatchedVariables1);
//    }

    private void intersectStringLiterals() {
        this.unmatchedStringLiterals1.addAll(statement1.getStringLiterals());
        this.unmatchedStringLiterals2.addAll(statement2.getStringLiterals());
        ReplacementUtil.removeCommonElements(unmatchedStringLiterals1, unmatchedStringLiterals2);
    }

    private void intersectArgumentIdentifiers() {
        this.unmatchedArguments1 = new LinkedHashSet<>(statement1.getIdentifierArguments());
        this.unmatchedArguments2 = new LinkedHashSet<>(statement2.getIdentifierArguments());
        ReplacementUtil.removeCommonElements(unmatchedArguments1, unmatchedArguments2);
    }

    private void intersectNumberLiterals() {
        this.unmatchedNumberLiterals1 = new LinkedHashSet<>(statement1.getNumberLiterals());
        this.unmatchedNumberLiterals2 = new LinkedHashSet<>(statement2.getNumberLiterals());
        ReplacementUtil.removeCommonElements(unmatchedNumberLiterals1, unmatchedNumberLiterals2);
    }

    public Set<String> getUnmatchedStringLiterals1() {
        return this.unmatchedStringLiterals1;
    }

    public Set<String> getUnmatchedStringLiterals2() {
        return this.unmatchedStringLiterals2;
    }

    public Set<String> getUnmatchedNumberLiterals1() {
        return this.unmatchedNumberLiterals1;
    }

    public Set<String> getUnmatchedNumberLiterals2() {
        return this.unmatchedNumberLiterals2;
    }

    public Set<String> getUnmatchedArguments1() {
        return this.unmatchedArguments1;
    }

    public Set<String> getUnmatchedArguments2() {
        return this.unmatchedArguments2;
    }

    public Set<String> getUnmatchedVariables1() {
        return this.unmatchedVariables1;
    }

    public Set<String> getUnmatchedVariables2() {
        return this.unmatchedVariables2;
    }

    public void setUnmatchedVariables1(Set<String> variables1) {
        this.unmatchedArguments1 = variables1;
    }

    public void setUnmatchedVariables2(Set<String> variables2) {
        this.unmatchedArguments1 = variables2;
    }
}
