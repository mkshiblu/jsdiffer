package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.FunctionDeclaration;
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

        if (o.getFragment1().getString().equals(o.getFragment2().getString())) {
            distance2 = 0;
        } else {
            String s1 = o.getFragment1().getString().toLowerCase();
            String s2 = o.getFragment2().getString().toLowerCase();
            int distance = StringDistance.editDistance(s1, s2);
            distance2 = (double) distance / (double) Math.max(s1.length(), s2.length());
        }

        if (distance1 != distance2) {
            return Double.compare(distance1, distance2);
        } else {
            if (this.compositeChildMatchingScore != o.compositeChildMatchingScore) {
                return -Double.compare(this.compositeChildMatchingScore, o.compositeChildMatchingScore);
            } else {
                int depthDiff1 = Math.abs(this.getFragment1().getDepth() - this.getFragment2().getDepth());
                int depthDiff2 = Math.abs(o.getFragment1().getDepth() - o.getFragment2().getDepth());

                if (depthDiff1 != depthDiff2) {
                    return Integer.valueOf(depthDiff1).compareTo(Integer.valueOf(depthDiff2));
                } else {
                    int indexDiff1 = Math.abs(this.getFragment1().getIndex() - this.getFragment2().getIndex());
                    int indexDiff2 = Math.abs(o.getFragment1().getIndex() - o.getFragment2().getIndex());
                    return Integer.valueOf(indexDiff1).compareTo(Integer.valueOf(indexDiff2));
                }
            }
        }
    }
}
