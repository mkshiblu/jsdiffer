package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.uml.mapping.replacement.ReplacementUtil;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Contains all the unmatched arguments, literals, variables etc.
 */
public class CodeFragmentDiff {
    public final CodeFragment statement1;
    public final CodeFragment statement2;
    public final Set<String> stringLiterals1 = new LinkedHashSet<>();
    public final Set<String> stringLiterals2 = new LinkedHashSet<>();
    public final Set<String> numberLiterals1 = new LinkedHashSet<>();
    public final Set<String> numberLiterals2 = new LinkedHashSet<>();
    public final Set<String> booleanLiterals1 = new LinkedHashSet<>();
    public final Set<String> booleanLiterals2 = new LinkedHashSet<>();
    public final Set<String> arguments1 = new LinkedHashSet<>();
    public final Set<String> arguments2 = new LinkedHashSet<>();
    public final Set<String> variables1 = new LinkedHashSet<>();
    public final Set<String> variables2 = new LinkedHashSet<>();
    public final Set<String> arrayAccesses1 = new LinkedHashSet<>();
    public final Set<String> arrayAccesses2 = new LinkedHashSet<>();
    public final Set<String> prefixExpressions1 = new LinkedHashSet<>();
    public final Set<String> prefixExpressions2 = new LinkedHashSet<>();

    public CodeFragmentDiff(CodeFragment statement1, CodeFragment statement2) {
        this.statement1 = statement1;
        this.statement2 = statement2;
        intersectVariables();
        intersectStringLiterals();
        intersectNumberLiterals();
        intersectBooleanLiterals();
        intersectArguments();
        intersectArrayAccess();
        intersectPrefixExpressions();
    }

    private void intersectVariables() {
        variables1.addAll(statement1.getVariables());
        variables2.addAll(statement2.getVariables());
        ReplacementUtil.removeCommonElements(variables1, variables2);
    }

    private void intersectStringLiterals() {
        this.stringLiterals1.addAll(statement1.getStringLiterals());
        this.stringLiterals2.addAll(statement2.getStringLiterals());
        ReplacementUtil.removeCommonElements(stringLiterals1, stringLiterals2);
    }

    private void intersectArguments() {
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

    private void intersectBooleanLiterals() {
        this.booleanLiterals1.addAll(statement1.getBooleanLiterals());
        this.booleanLiterals2.addAll(statement2.getBooleanLiterals());
        ReplacementUtil.removeCommonElements(this.booleanLiterals1, this.booleanLiterals2);
    }

    @Override
    public String toString() {
        return "S1: " + statement1 + System.lineSeparator() + " S2: " + statement2;
    }
}
