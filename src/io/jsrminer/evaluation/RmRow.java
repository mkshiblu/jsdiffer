package io.jsrminer.evaluation;

public class RmRow extends Ref {
    String refactoring;

    @Override
    public String toString() {
        return refType.toString() + '\t' + refactoring;
    }
}
