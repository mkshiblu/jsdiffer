package io.jsrminer.api;

import java.util.List;

public interface IGitHistoryMiner {

    /**
     * Detects refactoring at the current commit
     *
     * @return
     */
    List<IRefactoring> detectAtCurrentCommit(String gitRepositoryPath);
}
