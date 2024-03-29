package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.Statement;
import io.jsrminer.uml.diff.StringDistance;

public class LeafCodeFragmentMapping extends CodeFragmentMapping implements Comparable<LeafCodeFragmentMapping> {

    public LeafCodeFragmentMapping(CodeFragment statement1, CodeFragment statement2, FunctionDeclaration function1, FunctionDeclaration function2, Argumentizer argumentizer) {
        super(statement1, statement2, function1, function2, argumentizer);
    }

    @Override
    public int compareTo(LeafCodeFragmentMapping o) {

        // THe one
        // The absolute difference
        if (this.atLeastOneFragmentContainsAnonymous() || o.atLeastOneFragmentContainsAnonymous()){
            int diff1 = this.absoluteDifferenceInAnonymousCount();
            int diff2 = o.absoluteDifferenceInAnonymousCount();
            // If we have the mapping
            // The one with more mappongs inside the anonymous classes (ExactMatching / expreminet)


            return Integer.compare(diff1, diff2);
        }

        double distance1 = this.normalizedTextualDistance();
        double distance2 = o.normalizedTextualDistance();

        if (distance1 != distance2) {
//            if (this.isIdenticalWithExtractedVariable() && !o.isIdenticalWithExtractedVariable()) {
//                return -1;
//            } else if (!this.isIdenticalWithExtractedVariable() && o.isIdenticalWithExtractedVariable()) {
//                return 1;
//            }
//            if (this.isIdenticalWithInlinedVariable() && !o.isIdenticalWithInlinedVariable()) {
//                return -1;
//            } else if (!this.isIdenticalWithInlinedVariable() && o.isIdenticalWithInlinedVariable()) {
//                return 1;
//            }
            return Double.compare(distance1, distance2);
        } else {
            int depthDiff1 = Math.abs(this.fragment1.getDepth() - this.fragment2.getDepth());
            int depthDiff2 = Math.abs(o.fragment1.getDepth() - o.fragment2.getDepth());

            if (depthDiff1 != depthDiff2) {
                return Integer.valueOf(depthDiff1).compareTo(Integer.valueOf(depthDiff2));
            } else {
                int indexDiff1 = Math.abs(this.fragment1.getPositionIndexInParent() - this.fragment2.getPositionIndexInParent());
                int indexDiff2 = Math.abs(o.fragment1.getPositionIndexInParent() - o.fragment2.getPositionIndexInParent());
                if (indexDiff1 != indexDiff2) {
                    return Integer.valueOf(indexDiff1).compareTo(Integer.valueOf(indexDiff2));
                } else {
                    double parentEditDistance1 = this.normalizedParentEditDistance();
                    double parentEditDistance2 = o.normalizedParentEditDistance();
                    return Double.compare(parentEditDistance1, parentEditDistance2);
                }
            }
        }
    }

    /**
     * The Levenshtein distance between two words is the minimum number
     * of single-character edits (insertions, deletions or substitutions) required to change one word into the other.
     * This function returns the normalized edit distance i.e. Levenshtein distance / length of
     * the longest string between the two statements. When calculating the edit distance, it uses the lower cased version of
     * the two statement
     */
    double normalizedTextualDistance() {
        final String text1 = fragment1.getText();
        final String text2 = fragment2.getText();
        final double distance;
        if (text1.equals(text2)) {
            distance = 0;
        } else {
            String s1 = text1.toLowerCase();
            String s2 = text2.toLowerCase();
            int editDistance = StringDistance.editDistance(s1, s2);
            distance = (double) editDistance / (double) Math.max(s1.length(), s2.length());
        }
        return distance;
    }

    double normalizedParentEditDistance() {
        Statement parent1 = fragment1.getParent();
        while (parent1 != null && parent1.getCodeElementType().equals(CodeElementType.BLOCK_STATEMENT)) {
            parent1 = parent1.getParent();
        }
        Statement parent2 = fragment2.getParent();
        while (parent2 != null && parent2.getCodeElementType().equals(CodeElementType.BLOCK_STATEMENT)) {
            parent2 = parent2.getParent();
        }

        if (parent1 == null && parent2 == null) {
            //method signature is the parent
            return 0;
        } else if (parent1 == null && parent2 != null) {
            String s2 = parent2.getText();
            int distance = StringDistance.editDistance("{", s2);
            double normalized = (double) distance / Math.max(1, s2.length());
            return normalized;
        } else if (parent1 != null && parent2 == null) {
            String s1 = parent1.getText();
            int distance = StringDistance.editDistance(s1, "{");
            double normalized = (double) distance / Math.max(s1.length(), 1);
            return normalized;
        }
        String s1 = parent1.getText();
        String s2 = parent2.getText();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / Math.max(s1.length(), s2.length());
        return normalized;
    }
}
