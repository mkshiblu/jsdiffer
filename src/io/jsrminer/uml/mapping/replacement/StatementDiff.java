package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.SingleStatement;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Contains all the unmatched arguments, literals, variables etc.
 */
public class StatementDiff {
    public final SingleStatement statement1;
    public final SingleStatement statement2;
    public final Set<String> stringLiterals1 = new LinkedHashSet<>();
    public final Set<String> stringLiterals2 = new LinkedHashSet<>();
    public final Set<String> numberLiterals1 = new LinkedHashSet<>();
    public final Set<String> numberLiterals2 = new LinkedHashSet<>();
    public final Set<String> arguments1 = new LinkedHashSet<>();
    public final Set<String> arguments2 = new LinkedHashSet<>();
    public final Set<String> variables1 = new LinkedHashSet<>();
    public final Set<String> variables2 = new LinkedHashSet<>();
    public final Set<String> arrayAccesses1 = new LinkedHashSet<>();
    public final Set<String> arrayAccesses2 = new LinkedHashSet<>();
    public final Set<String> prefixExpressions1 = new LinkedHashSet<>();
    public final Set<String> prefixExpressions2 = new LinkedHashSet<>();

    public StatementDiff(SingleStatement statement1, SingleStatement statement2) {
        this.statement1 = statement1;
        this.statement2 = statement2;
        intersectVariables();
        intersectStringLiterals();
        intersectNumberLiterals();
        intersectArgumentIdentifiers();
        intersectArrayAccess();
        intersectPrefixExpressions();
    }

    private void intersectVariables() {
        variables1.addAll(statement1.getVariables());
        variables2.addAll(statement2.getVariables());
        ReplacementUtil.removeCommonElements(variables1, variables1);
    }

    private void intersectStringLiterals() {
        this.stringLiterals1.addAll(statement1.getStringLiterals());
        this.stringLiterals2.addAll(statement2.getStringLiterals());
        ReplacementUtil.removeCommonElements(stringLiterals1, stringLiterals2);
    }

    private void intersectArgumentIdentifiers() {
        this.arguments1.addAll(statement1.getIdentifierArguments());
        this.arguments2.addAll(statement2.getIdentifierArguments());
        ReplacementUtil.removeCommonElements(arguments1, arguments2);
    }

    private void intersectNumberLiterals() {
        this.numberLiterals1.addAll(statement1.getNumberLiterals());
        this.numberLiterals2.addAll(statement2.getNumberLiterals());
        ReplacementUtil.removeCommonElements(numberLiterals1, numberLiterals2);
    }

    private void intersectPrefixExpressions() {
        prefixExpressions1.addAll(statement1.getPrefixExpressions());
        prefixExpressions2.addAll(statement2.getPrefixExpressions());
        ReplacementUtil.removeCommonElements(prefixExpressions1, prefixExpressions2);
    }

    private void intersectArrayAccess() {
        this.arrayAccesses1.addAll(statement1.getArrayAccesses());
        this.arrayAccesses2.addAll(statement2.getArrayAccesses());
        ReplacementUtil.removeCommonElements(arrayAccesses1, arrayAccesses2);
    }

//
//    public Set<String> getUnmatchedStringLiterals1() {
//        return this.unmatchedStringLiterals1;
//    }
//
//    public Set<String> getUnmatchedStringLiterals2() {
//        return this.unmatchedStringLiterals2;
//    }
//
//    public Set<String> getUnmatchedNumberLiterals1() {
//        return this.unmatchedNumberLiterals1;
//    }
//
//    public Set<String> getUnmatchedNumberLiterals2() {
//        return this.unmatchedNumberLiterals2;
//    }
//
//    public Set<String> getUnmatchedArguments1() {
//        return this.unmatchedArguments1;
//    }
//
//    public Set<String> getUnmatchedArguments2() {
//        return this.unmatchedArguments2;
//    }
//
//    public Set<String> getUnmatchedVariables1() {
//        return this.unmatchedVariables1;
//    }
//
//    public Set<String> getUnmatchedVariables2() {
//        return this.unmatchedVariables2;
//    }
//
//    public void setUnmatchedVariables1(Set<String> variables1) {
//        this.unmatchedArguments1 = variables1;
//    }
//
//    public void setUnmatchedVariables2(Set<String> variables2) {
//        this.unmatchedArguments1 = variables2;
//    }
}
