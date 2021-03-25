package io.jsrminer.evaluation;

public class RdRow extends Ref {
    private String refactoring;
    String nodeType;

    public String getRefactoring() {
        return refactoring;
    }

    public void setRefactoring(String refactoring) {
        this.refactoring = refactoring;
    }

//    @Override
//    public String toString() {
//        return "(" + getLocationAfter().start + "-" + getLocationAfter().end + ") " + commit;
//    }
}
