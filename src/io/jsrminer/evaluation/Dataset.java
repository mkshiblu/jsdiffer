package io.jsrminer.evaluation;

import java.util.*;
import java.util.stream.Stream;

public class Dataset {
    private Map<String, RepositoryCommits> repositoryCommitsMap = new HashMap<>();
    private List<Ref> refactorings = new ArrayList<>();

    void addRefactoring(Ref refactoring) {
        this.refactorings.add(refactoring);
        var repositoryCommits
                = repositoryCommitsMap
                .computeIfAbsent(refactoring.repository, mapper -> new RepositoryCommits(refactoring.repository));

        repositoryCommits.registerRefactoring(refactoring.commit, refactoring);
    }

    public List<Ref> getRefsInRepositoryCommit(String repository, String commitId) {
        return repositoryCommitsMap.get(repository).getCommitRefactorings(commitId);
    }

    int getRefactoringsCount() {
        return (int) repositoryCommitsMap.values()
                .stream()
                .flatMap(repositoryCommits -> Stream.of(repositoryCommits.refactoringsCount()))
                .count();
    }


    public Map<String, RepositoryCommits> getRepositoryCommitsMap() {
        return repositoryCommitsMap;
    }

    public List<Ref> getRefactorings() {
        return refactorings;
    }

    @Override
    public String toString() {
        return "Ref Count : " + getRefactoringsCount();
    }
}
