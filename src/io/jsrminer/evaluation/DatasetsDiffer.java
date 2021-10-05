package io.jsrminer.evaluation;

import java.util.*;

public class DatasetsDiffer {
    private final Dataset dataSet1;
    private final Dataset dataSet2;

    public DatasetsDiffer(Dataset dataSet1, Dataset dataSet2) {
        this.dataSet1 = dataSet1;
        this.dataSet2 = dataSet2;
    }

    public DatasetDiff diff() {
        DatasetDiff diff = new DatasetDiff(dataSet1, dataSet2);
        for (var repository1Entry :
                dataSet1.getRepositoryCommitsMap().entrySet()) {
            var repository1 = repository1Entry.getKey();
            var repository1Commits = repository1Entry.getValue();
            var repository2Commits = dataSet2.getRepositoryCommitsMap().get(repository1);

            if (repository2Commits == null) {
                //not found
                diff.registerRepositoryNotReportedByDataset2(repository1);
            } else {
                // Both datasets reported atleast one refactoring on the same project
                for (var commit : repository1Commits.getCommits()) {
                    var commitRefactorings2 = repository2Commits.getCommitRefactorings(commit);
                    if (commitRefactorings2 == null) {
                        // not found
                        diff.registerCommitNotReportedByDataset2(repository1, commit);
                    } else {
                        // Common commit
                        var commitDiff = diffRepositoryCommit(repository1, commit);
                        diff.registerCommitDiff(commitDiff, repository1);
                    }
                }
            }
        }
        return diff;
    }

    public CommitRefactoringsDiff diffRepositoryCommit(String repository, String commit) {
        var refactorings1 = dataSet1.getRefsInRepositoryCommit(repository, commit);
        var refactorings2 = dataSet2.getRefsInRepositoryCommit(repository, commit);
        var result = diffCommitRefs(refactorings1, refactorings2);
        return result;
    }

    private CommitRefactoringsDiff diffCommitRefs(List<Ref> rdRefs, List<Ref> rmRefs) {
        var rdRefTypeMap = new HashMap<Ref.RefType, List<Ref>>();
        var rmRefTypeMap = new HashMap<Ref.RefType, List<Ref>>();

        for (var rdRow : rdRefs) {
            rdRefTypeMap
                    .computeIfAbsent(rdRow.refType, key -> new ArrayList<>())
                    .add(rdRow);
        }

        for (var ref : rmRefs) {
            rmRefTypeMap
                    .computeIfAbsent(ref.refType, key -> new ArrayList<>())
                    .add(ref);
        }

        CommitRefactoringsDiff result = diffRefTypeMaps(rdRefTypeMap, rmRefTypeMap);

        return result;
    }

    private CommitRefactoringsDiff diffRefTypeMaps(Map<Ref.RefType, List<Ref>> rdRefTypeMap, Map<Ref.RefType, List<Ref>> rmRefTypeMap) {
        var commitID = rdRefTypeMap.values().stream().findAny().get().get(0).commit;
        CommitRefactoringsDiff result = new CommitRefactoringsDiff(commitID);

        for (var rdEntry : rdRefTypeMap.entrySet()) {
            var refType = rdEntry.getKey();
            var rdRefactorings = rdRefTypeMap.get(refType);
            var rmRefactorings = rmRefTypeMap.get(refType);

            if (rmRefactorings != null) {
                matchSameTypeRefactorings(rdRefactorings, rmRefactorings, result);
            } else {
                result.registerCompletelyUnmatchedTypeRefsInRd(refType, rdRefactorings);
            }
        }

        return result;
    }

    private void matchSameTypeRefactorings(List<Ref> rdRefactorings, List<Ref> rmRefactorings, CommitRefactoringsDiff result) {
        boolean matchFound;
        Ref rdRef;
        Ref rmRef;

        for (ListIterator<Ref> rdIterator = rdRefactorings.listIterator(); rdIterator.hasNext(); ) {
            matchFound = false;
            rdRef = rdIterator.next();

            for (ListIterator<Ref> rmIterator = rmRefactorings.listIterator(); rmIterator.hasNext(); ) {
                rmRef = rmIterator.next();
                if (RefactoringMatcher.isMatch(rdRef, rmRef)) {
                    matchFound = true;
                    rdRef.setValidationType(ValidationType.TruePositive);
                    rmRef.setValidationType(ValidationType.TruePositive);
                    rmIterator.remove();
                    rdIterator.remove();
                    break;
                }
            }

            if (matchFound) {
                result.registerMatched(rdRef);
            } else {
                result.registerUnmatchedInRd(rdRef);
            }
        }
    }
}
