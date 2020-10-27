package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.ObjectCreation;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.uml.mapping.replacement.InvocationCoverage;
import io.jsrminer.uml.mapping.replacement.Replacement;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class CodeFragmentMapping {
    public final CodeFragment fragment1;
    public final CodeFragment fragment2;

    private Set<Replacement> replacements = new LinkedHashSet<>();

    public CodeFragmentMapping(CodeFragment fragment1, CodeFragment fragment2) {
        this.fragment1 = fragment1;
        this.fragment2 = fragment2;
    }

    public abstract boolean isExactMatch();

    @Override
    public String toString() {
        return fragment1.toString() + fragment2.toString();
    }

    public void addReplacements(Set<Replacement> replacements) {
        this.replacements.addAll(replacements);
    }

    public Set<Replacement> getReplacements() {
        return replacements;
    }

    public boolean isExact(Argumentizer argumentizer) {
        return !isKeyword() && (fragment1.getText().equals(fragment2.getText())
                || argumentizer.getArgumentizedString(fragment1).equals(argumentizer.getArgumentizedString(fragment2))
                || isExactAfterAbstraction() || containsIdenticalOrCompositeReplacement());
    }

    private boolean isExactAfterAbstraction() {
        OperationInvocation invocation1 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(fragment1);
        OperationInvocation invocation2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(fragment2);
        if (invocation1 != null && invocation2 != null) {
            return invocation1.actualString().equals(invocation2.actualString());
        }
        ObjectCreation creation1 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(fragment1);
        ObjectCreation creation2 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(fragment2);
        if (creation1 != null && creation2 != null) {
            return creation1.actualString().equals(creation2.actualString());
        }
        return false;
    }

    private boolean containsIdenticalOrCompositeReplacement() {
        for (Replacement r : replacements) {
            if (r.getType().equals(Replacement.ReplacementType.ARRAY_INITIALIZER_REPLACED_WITH_METHOD_INVOCATION_ARGUMENTS) &&
                    r.getBefore().equals(r.getAfter())) {
                return true;
            } else if (r.getType().equals(Replacement.ReplacementType.COMPOSITE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKeyword() {
        return fragment1.getText().startsWith("return;") ||
                fragment1.getText().startsWith("break;") ||
                fragment1.getText().startsWith("continue;");
    }

    public boolean containsReplacement(Replacement.ReplacementType type) {
        for (Replacement replacement : replacements) {
            if (replacement.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean equalFragmentText(Argumentizer argumentizer) {
        String text1 = fragment1.getText();
        String text2 = fragment2.getText();

        boolean containsOrEqualText = text1.contains(text2) || text2.contains(text1);
        if (containsOrEqualText)
            return true;

        String argumentizedText1 = argumentizer.getArgumentizedString(fragment1);
        String argumentizedText2 = argumentizer.getArgumentizedString(fragment2);
        boolean equalTextWithArgumentization = (argumentizedText1 != null && argumentizedText1.equals(text2))
                || (argumentizedText2 != null && argumentizedText2.equals(text1));

        return equalTextWithArgumentization;
    }
}
