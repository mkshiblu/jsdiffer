package io.jsrminer.evaluation;

import java.util.*;
import java.util.stream.Stream;

public class RepositoryCommits {
    private final String repositoryName;
    private final Map<String, List<Ref>> commitRefactoringsMap = new LinkedHashMap<>();

    public RepositoryCommits(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public void registerRefactoring(String commit, Ref refactoring) {
        commitRefactoringsMap.computeIfAbsent(commit, v -> new ArrayList<>())
                .add(refactoring);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public Set<String> getCommits() {
        return commitRefactoringsMap.keySet();
    }

    public List<Ref> getCommitRefactorings(String commit) {
        return commitRefactoringsMap.get(commit);
    }

    public int commitsCount() {
        return getCommits().size();
    }

    public int refactoringsCount() {
        var count = commitRefactoringsMap.values()
                .stream()
                .flatMap(refs -> Stream.of(refs.size()))
                .count();

        return (int) count;
    }
}
