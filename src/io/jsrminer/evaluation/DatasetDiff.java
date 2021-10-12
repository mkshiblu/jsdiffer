package io.jsrminer.evaluation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatasetDiff {

    List<String> projectNotReportedByRm = new ArrayList<>();
    Map<String, List<String>> projectCommitsNotReportedByRm = new LinkedHashMap<>();
    Map<String, List<CommitRefactoringsDiff>> commitDiffs = new LinkedHashMap<>();
    private final Dataset dataset1;
    private final Dataset dataset2;

    public DatasetDiff(Dataset dataset1, Dataset dataset2) {

        this.dataset1 = dataset1;
        this.dataset2 = dataset2;
    }

    public void registerRepositoryNotReportedByDataset2(String project) {
        this.projectNotReportedByRm.add(project);
    }

//    private final List<Ref> commonRefactorings = new ArrayList<>();
//
//    public void addCommonRefactoring(Ref refactoring) {
//        commonRefactorings.add(refactoring);
//    }

    public List<Ref> getProjectCommonRefactorings(String projectName) {
        var x = commitDiffs.get(projectName)
                .stream()
                .map(diff -> diff.getAllMatched())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return x;
    }

    public List<Ref> getCommonRefactorings() {
        return dataset1.getRepositoryNames()
                .stream()
                .map(project -> getProjectCommonRefactorings(project))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

    }

    public void registerCommitNotReportedByDataset2(String project, String commit) {
        this.projectCommitsNotReportedByRm
                .computeIfAbsent(project, mapping -> new ArrayList<>())
                .add(commit);
    }

    public void registerCommitDiff(CommitRefactoringsDiff commitDiff, String repositoryName) {
        this.commitDiffs
                .computeIfAbsent(repositoryName, mapper -> new ArrayList<>())
                .add(commitDiff);
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


        for (var entry : commitDiffs.entrySet()) {
            var diffs = entry.getValue();
            builder.append(entry.getKey() + " Common commits: " + diffs.size() + "\n");
            for (var diff : diffs) {
                var printer = new CommitRefactoringsDiffFormatter(diff);
                builder.append(printer.formatAsTable());
                builder.append("\n");
            }
        }

        builder.append("\n");
        return builder.toString();
    }
}
