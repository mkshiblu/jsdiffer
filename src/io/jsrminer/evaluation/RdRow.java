package io.jsrminer.evaluation;

public class RdRow {
    String repository;
    String commit;
    String refactoringName;
    String nodeType;
    String locationBefore;
    String localNameBefore;
    String locationAfter;
    String localNameAfter;

    public String getFileAfter(){
        return locationAfter.split(":")[0];
    }

    public String getRmRefactoringName(){
        return this.refactoringName + " " + nodeType == "Function" ? "method" : nodeType;
    }

    @Override
    public String toString() {
        return locationAfter + " " + commit;
    }
}
