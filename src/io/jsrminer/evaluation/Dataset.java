package io.jsrminer.evaluation;

import java.util.*;
import java.util.stream.Stream;

public class Dataset {
    private Map<String, RepositoryCommits> repositoryCommitsMap = new HashMap<>();
    private List<Ref> refactorings = new ArrayList<>();
    private final Set<String> repositoryNames = new TreeSet<>();

    void addRefactoring(Ref refactoring) {
        this.refactorings.add(refactoring);
        var repositoryCommits
                = repositoryCommitsMap
                .computeIfAbsent(refactoring.project, mapper -> new RepositoryCommits(refactoring.project));

        repositoryCommits.registerRefactoring(refactoring.commitId, refactoring);
    }

    public List<Ref> getRefsInRepositoryCommit(String repository, String commitId) {
        return repositoryCommitsMap.get(repository).getCommitRefactorings(commitId);
    }

    int getRefactoringsCount() {
        return  this.refactorings.size();
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

    public Set<String> getRepositoryNames() {
        return repositoryNames;
    }
}
