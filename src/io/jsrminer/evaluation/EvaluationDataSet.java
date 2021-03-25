package io.jsrminer.evaluation;

import java.util.*;

public class EvaluationDataSet<T extends Ref> {
    Map<String, Map<String, List<T>>> projectCommitsMap = new HashMap<>();
    List<T> rdRows = new LinkedList<>();
    Map<String, List<T>> commitRefMap = new HashMap<>();

    void reportRow(T rdRow) {
        this.rdRows.add(rdRow);

        var commitRMap = projectCommitsMap.computeIfAbsent(rdRow.repository
                , val -> new HashMap<>()
        );
        var commitRefs = commitRMap.computeIfAbsent(rdRow.commit, v -> new ArrayList<>());
        commitRefs.add(rdRow);

        commitRefMap.computeIfAbsent(rdRow.commit, v-> new ArrayList<>())
                .add(rdRow);
    }

    public List<T> getRefsInCommit(String commitId) {
        return commitRefMap.get(commitId);
    }

    int getRefactoringCount(){
        return rdRows.size();
    }

    @Override
    public String toString() {
        return "Ref Count : " + getRefactoringCount();
    }
}
