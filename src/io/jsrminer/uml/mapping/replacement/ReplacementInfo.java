package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.uml.diff.StringDistance;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds metadata between two strings which are subjects to replacements
 */
public class ReplacementInfo {
    public final String originalString1;
    public final String originalString2;
    final List<SingleStatement> unMatchedStatements1;
    final List<SingleStatement> unMatchedStatements2;

    private String argumentizedString1;
    private String argumentizedString2;
    private int rawEditDistance;

    private final Set<Replacement> appliedReplacements = new LinkedHashSet<>();

    //    public ReplacementFinder(String argumentizedString1, String argumentizedString2,
//                             List<SingleStatement> unMatchedStatements1, List<SingleStatement> unMatchedStatements2) {
//        this.argumentizedString2 = argumentizedString2;
//        setArgumentizedString1(argumentizedString1);
//        this.unMatchedStatements1 = unMatchedStatements1;
//        this.unMatchedStatements2 = unMatchedStatements2;
//        this.replacements = new LinkedHashSet<Replacement>();
//    }
    public ReplacementInfo(String argumentizedString1, String argumentizedString2,
                           List<SingleStatement> unMatchedStatements1, List<SingleStatement> unMatchedStatements2) {
        this.originalString1 = argumentizedString1;
        this.originalString2 = argumentizedString2;
        this.argumentizedString2 = argumentizedString2;
        this.unMatchedStatements1 = unMatchedStatements1;
        this.unMatchedStatements2 = unMatchedStatements2;
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

    public Set<Replacement> getAppliedReplacements() {
        return appliedReplacements;
    }

    public void addReplacement(Replacement replacement) {
        this.appliedReplacements.add(replacement);
    }

    public void addReplacements(Collection<? extends Replacement> replacementsToBeAdded) {
        this.appliedReplacements.addAll(replacementsToBeAdded);
    }

    public void removeReplacements(Collection<? extends Replacement> replacementsToBeRemoved) {
        this.appliedReplacements.removeAll(replacementsToBeRemoved);
    }
}