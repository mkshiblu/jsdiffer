package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.diff.StringDistance;

public class BlockCodeFragmentMapping extends CodeFragmentMapping implements Comparable<BlockCodeFragmentMapping> {

    private double compositeChildMatchingScore;

    public BlockCodeFragmentMapping(BlockStatement statement1, BlockStatement statement2, double childMatchScore
            , FunctionDeclaration operation1, FunctionDeclaration operation2, Argumentizer argumentizer) {
        super(statement1, statement2, operation1, operation2, argumentizer);
        this.compositeChildMatchingScore = childMatchScore;
    }


    @Override
    public int compareTo(BlockCodeFragmentMapping o) {
        double distance1 = this.getNormalizedTextualDistance();
        double distance2 = o.getNormalizedTextualDistance();

        final BlockStatement block1 = (BlockStatement) this.fragment1;
        final BlockStatement block2 = (BlockStatement) this.fragment2;

        if (distance1 != distance2) {
            return Double.compare(distance1, distance2);
        } else {
            if (this.compositeChildMatchingScore != o.compositeChildMatchingScore) {
                return -Double.compare(this.compositeChildMatchingScore, o.compositeChildMatchingScore);
            } else {
                int depthDiff1 = Math.abs(this.fragment1.getDepth() - this.fragment2.getDepth());
                int depthDiff2 = Math.abs(o.fragment1.getDepth() - o.fragment2.getDepth());

                if (depthDiff1 != depthDiff2) {
                    return Integer.valueOf(depthDiff1).compareTo(Integer.valueOf(depthDiff2));
                } else {
                    int indexDiff1 = Math.abs(this.fragment1.getPositionIndexInParent()
                            - this.fragment2.getPositionIndexInParent());
                    int indexDiff2 = Math.abs(o.fragment1.getPositionIndexInParent()
                            - o.fragment2.getPositionIndexInParent());
                    return Integer.valueOf(indexDiff1).compareTo(Integer.valueOf(indexDiff2));
                }
            }
        }
    }

    double getNormalizedTextualDistance() {
        final BlockStatement block1 = (BlockStatement) this.fragment1;
        final BlockStatement block2 = (BlockStatement) this.fragment2;

        if (block1.getTextWithExpressions().equals(block2.getTextWithExpressions())) {
            return 0;
        } else {
            String s1 = this.fragment1.getText().toLowerCase();
            String s2 = this.fragment2.getText().toLowerCase();
            int distance = StringDistance.editDistance(s1, s2);
            return (double) distance / (double) Math.max(s1.length(), s2.length());
        }
    }

    @Override
    public boolean isExactMatch() {
        return false;
    }
}
