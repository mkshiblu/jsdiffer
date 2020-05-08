package io.jsrminer;

import io.jsrminer.diff.SourceDirDiff;
import io.jsrminer.diff.SourceDirectory;
import org.eclipse.jgit.lib.Repository;
import org.jsrminer.api.GitHistoryRefactoringMiner;
import org.jsrminer.api.GitService;
import org.jsrminer.api.Refactoring;
import org.jsrminer.api.RefactoringHandler;
import org.jsrminer.util.JGitService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class JSRefactoringMiner {

    public static void detectBetweenDirectories(String previousVersionDirectory, String currentVersionDirectory) {
        SourceDirectory src1 = new SourceDirectory(previousVersionDirectory);
        SourceDirectory src2 = new SourceDirectory(currentVersionDirectory);
        SourceDirDiff diff = src1.diff(src2);

        System.out.println(Arrays.deepToString(diff.getCommonSourceFiles()));
        System.out.println("\nADDED: " + Arrays.deepToString(diff.getAddedFiles()));
        System.out.println("\nDeleted: " + Arrays.deepToString(diff.getDeletedFiles()));
    }

    public static void detectBetweenCommits(String folder, String startCommit, String endCommit) throws Exception {
        GitService gitService = new JGitService();
        try (Repository repo = gitService.openRepository(folder)) {
            Path folderPath = Paths.get(folder);
            String fileName = null;
            if (endCommit == null) {
                fileName = "refactorings_" + startCommit + "_begin" + ".csv";
            } else {
                fileName = "refactorings_" + startCommit + "_" + endCommit + ".csv";
            }
            String filePath = folderPath.toString() + "/" + fileName;
            Files.deleteIfExists(Paths.get(filePath));
            //saveToFile(filePath, getResultHeader());

            GitHistoryRefactoringMiner detector = null;//new GitHistoryRefactoringMinerImpl();
            detector.detectBetweenCommits(repo, startCommit, endCommit, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    if (refactorings.isEmpty()) {
                        System.out.println("No refactorings found in commit " + commitId);
                    } else {
                        System.out.println(refactorings.size() + " refactorings found in commit " + commitId);
                        for (Refactoring ref : refactorings) {
                            // saveToFile(filePath, getResultRefactoringDescription(commitId, ref));
                        }
                    }
                }

                @Override
                public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                    System.out.println("Finish mining, result is saved to file: " + filePath);
                    System.out.println(String.format("Total count: [Commits: %d, Errors: %d, Refactorings: %d]",
                            commitsCount, errorCommitsCount, refactoringsCount));
                }

                @Override
                public void handleException(String commit, Exception e) {
                    System.err.println("Error processing commit " + commit);
                    e.printStackTrace(System.err);
                }
            });
        }
    }
}
