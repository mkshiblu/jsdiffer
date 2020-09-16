package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.uml.diff.StringDistance;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Holds metadata between two strings which are subjects to replacements
 */
class ReplacementInfo {
    public final String originalString1;
    public final String originalString2;

    private String argumentizedString1;
    private String argumentizedString2;
    private int rawEditDistance;
    private final Set<Replacement> appliedReplacements = new LinkedHashSet<>();

    public ReplacementInfo(String argumentizedString1, String argumentizedString2) {
        this.originalString1 = argumentizedString1;
        this.originalString2 = argumentizedString2;
        this.argumentizedString2 = argumentizedString2;
        setArgumentizedString1(argumentizedString1);
    }

    public void setArgumentizedString1(String string) {
        this.argumentizedString1 = string;
        this.rawEditDistance = StringDistance.editDistance(this.argumentizedString1, this.argumentizedString2);
    }

    public int getRawEditDistance() {
        return rawEditDistance;
    }

    public String getArgumentizedString1() {
        return argumentizedString1;
    }

    public String getArgumentizedString2() {
        return argumentizedString2;
    }

    public Set<Replacement> getReplacements() {
        return appliedReplacements;
    }
}
