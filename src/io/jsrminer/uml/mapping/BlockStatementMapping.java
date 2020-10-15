package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.uml.diff.StringDistance;

public class BlockStatementMapping extends StatementMapping implements Comparable<BlockStatementMapping> {

    private double compositeChildMatchingScore;
    private BlockStatement block1;
    private BlockStatement block2;

    public BlockStatementMapping(BlockStatement statement1, BlockStatement statement2, double childMatchScore
            /*,FunctionDeclaration operation1, FunctionDeclaration operation2*/) {
        super(statement1, statement2/*, operation1, operation2*/);
        this.compositeChildMatchingScore = childMatchScore;
        this.block1 = statement1;
        this.block2 = statement2;
    }

    @Override
    public int compareTo(BlockStatementMapping o) {
        double distance1;
        double distance2;

        if (this.block1.getTextWithExpressions().equals(this.block2.getTextWithExpressions())) {
            distance1 = 0;
        } else {
            String s1 = this.statement1.getText().toLowerCase();
            String s2 = this.statement2.getText().toLowerCase();
            int distance = StringDistance.editDistance(s1, s2);
            distance1 = (double) distance / (double) Math.max(s1.length(), s2.length());
        }

        if (o.block1.getTextWithExpressions().equals(o.block2.getTextWithExpressions())) {
            distance2 = 0;
        } else {
            String s1 = o.block1.getTextWithExpressions().toLowerCase();
            String s2 = o.block2.getTextWithExpressions().toLowerCase();
            int distance = StringDistance.editDistance(s1, s2);
            distance2 = (double) distance / (double) Math.max(s1.length(), s2.length());
        }

        if (distance1 != distance2) {
            return Double.compare(distance1, distance2);
        } else {
            if (this.compositeChildMatchingScore != o.compositeChildMatchingScore) {
                return -Double.compare(this.compositeChildMatchingScore, o.compositeChildMatchingScore);
            } else {
                int depthDiff1 = Math.abs(this.statement1.getDepth() - this.statement2.getDepth());
                int depthDiff2 = Math.abs(o.statement1.getDepth() - o.statement2.getDepth());

                if (depthDiff1 != depthDiff2) {
                    return Integer.valueOf(depthDiff1).compareTo(Integer.valueOf(depthDiff2));
                } else {
                    int indexDiff1 = Math.abs(this.statement1.getPositionIndexInParent()
                            - this.statement2.getPositionIndexInParent());
                    int indexDiff2 = Math.abs(o.statement1.getPositionIndexInParent()
                            - o.statement2.getPositionIndexInParent());
                    return Integer.valueOf(indexDiff1).compareTo(Integer.valueOf(indexDiff2));
                }
            }
        }
    }

    @Override
    public boolean isExactMatch() {
        return false;
    }
}
