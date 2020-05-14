package io.jsrminer.api;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.jsrminer.api.Churn;

import java.util.List;
import java.util.Map;

public interface IGitService {
    /**
     * Clone the git repository given by {@code cloneUrl} only if is does not exist yet in {@code folder}.
     *
     * @param folder   The folder to store the local repo.
     * @param cloneUrl The repository URL.
     * @return The repository object (JGit library).
     * @throws Exception propagated from JGit library.
     */
    Repository cloneIfNotExists(String folder, String cloneUrl/*, String branch*/) throws Exception;

    Repository openRepository(String folder) throws Exception;

    int countCommits(Repository repository, String branch) throws Exception;

    void checkout(Repository repository, String commitId) throws Exception;

    RevWalk fetchAndCreateNewRevsWalk(Repository repository) throws Exception;

    RevWalk fetchAndCreateNewRevsWalk(Repository repository, String branch) throws Exception;

    RevWalk createAllRevsWalk(Repository repository) throws Exception;

    RevWalk createAllRevsWalk(Repository repository, String branch) throws Exception;

    Iterable<RevCommit> createRevsWalkBetweenTags(Repository repository, String startTag, String endTag) throws Exception;

    Iterable<RevCommit> createRevsWalkBetweenCommits(Repository repository, String startCommitId, String endCommitId) throws Exception;

   // static void fileTreeDiff(Repository repository, RevCommit currentCommit, List<String> filesBefore, List<String> filesCurrent, Map<String, String> renamedFilesHint) throws Exception;

    Churn churn(Repository repository, RevCommit currentCommit) throws Exception;
}
