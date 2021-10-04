package io.jsrminer.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CommitRefactorings {
    private List<Ref> refactorings = new ArrayList<>();
    private String commit;

    public CommitRefactorings(String commit) {
        this.commit = commit;
    }

    public void registerRefactoring(Ref refactoring) {
        this.refactorings.add(refactoring);
    }

    public List<Ref> getRefactorings() {
        return this.refactorings;
    }
}
