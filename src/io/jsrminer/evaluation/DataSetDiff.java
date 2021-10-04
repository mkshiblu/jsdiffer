package io.jsrminer.evaluation;

import java.util.*;

public class DataSetDiff {

    List<String> projectNotReportedByRm = new ArrayList<>();
    Map<String, List<String>> projectCommitsNotReportedByRm = new LinkedHashMap<>();
    List<CommitRefactoringsDiff> commitDiffs = new ArrayList<>();

    public void registerRepositoryNotReportedByDataset2(String project) {
        this.projectNotReportedByRm.add(project);
    }

    public void registerCommitNotReportedByDataset2(String project, String commit) {
        this.projectCommitsNotReportedByRm
                .computeIfAbsent(project, mapping -> new ArrayList<>())
                .add(commit);
    }

    public void registerCommitDiff(CommitRefactoringsDiff commitDiff) {
        this.commitDiffs.add(commitDiff);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(128000);
        builder.append(projectNotReportedByRm.size() + " Missed Projects: ");
        builder.append(projectNotReportedByRm.toString());
        builder.append("\n");

        builder.append("Missed Commits: \n");
        for (var project : projectCommitsNotReportedByRm.keySet()) {
            var commits = projectCommitsNotReportedByRm.get(project);
            //var commitsMissed = String.join(",", commits);
            builder.append(commits.size() + " in ");
            builder.append(project);
            builder.append("\t");
            builder.append(commits.toString());
            builder.append("\n");
        }

        builder.append("Refactorings at " + commitDiffs.size() + " Commits: ");
        builder.append("\n");

        //builder.append("commit\tMatchedCount\tUnmatchedCount");
        for (var diff : commitDiffs) {
            var printer = new CommitRefactoringsDiffFormatter(diff);
            builder.append(printer.formatAsTable());
            builder.append("\n");
        }

        return builder.toString();
    }
}
