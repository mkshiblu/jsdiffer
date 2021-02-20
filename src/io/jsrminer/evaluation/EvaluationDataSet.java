package io.jsrminer.evaluation;

import java.util.*;

public class EvaluationDataSet {
    Map<String, Map<String, List<RdRow>>> projectCommitsMap = new HashMap<>();
    List<RdRow> rdRows = new LinkedList<>();
    Map<String, List<RdRow>> commitRefMap = new HashMap<>();

    void reportRow(RdRow rdRow) {
        this.rdRows.add(rdRow);

        var commitRMap = projectCommitsMap.computeIfAbsent(rdRow.repository
                , val -> new HashMap<>()
        );
        var commitRefs = commitRMap.computeIfAbsent(rdRow.commit, v -> new ArrayList<>());
        commitRefs.add(rdRow);

        commitRefMap.computeIfAbsent(rdRow.commit, v-> new ArrayList<>())
                .add(rdRow);
    }

    public List<RdRow> getRefsInCommit(String commitId) {
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
