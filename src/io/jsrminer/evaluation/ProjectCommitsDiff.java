package io.jsrminer.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a diff between a common project and their diff
 */
public class ProjectCommitsDiff {

    private final List<CommitRefactoringsDiff> commitDiffs = new ArrayList<>();
    private final String projectName;

    public ProjectCommitsDiff(String projectName) {
        this.projectName = projectName;
    }

    public void registerCommitDiff(CommitRefactoringsDiff commitDiff) {
        this.commitDiffs.add(commitDiff);
    }

    @Override
    public String toString() {
        return projectName;
    }

    public List<CommitRefactoringsDiff> getCommitDiffs() {
        return commitDiffs;
    }

    public String getProjectName() {
        return projectName;
    }

//    public List<Ref> refactoringsOnlyInDataset1() {
//
//    }
//
//    public List<String> onlyInDataset2() {
//
//    }
//
//    public List<String> commonCommits() {
//
//    }
//
//    public List<Ref> commonRefactorings() {
//
//    }
//
//    public float precision(){
//
//    }
//
//    public float recall(){
//        var diff = commitDiffs.get(0);
//
//        diff.
//    }
}
