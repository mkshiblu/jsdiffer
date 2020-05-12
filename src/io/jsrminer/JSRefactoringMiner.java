package io.jsrminer;

import io.jsrminer.api.IGitService;
import io.jsrminer.diff.SourceDirDiff;
import io.jsrminer.diff.SourceDirectory;
import io.jsrminer.io.GitUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class JSRefactoringMiner {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void detectBetweenDirectories(String previousVersionDirectory, String currentVersionDirectory) {
        SourceDirectory src1 = new SourceDirectory(previousVersionDirectory);
        SourceDirectory src2 = new SourceDirectory(currentVersionDirectory);
        SourceDirDiff diff = src1.diff(src2);

        System.out.println("Common Files: " + Arrays.deepToString(diff.getCommonSourceFiles()));
        System.out.println("\nADDED: " + Arrays.deepToString(diff.getAddedFiles()));
        System.out.println("\nDeleted: " + Arrays.deepToString(diff.getDeletedFiles()));

        // TODO unfinished
    }

    public static void detectBetweenCommits(Repository repository, String startCommitId, String endCommitId) throws Exception {
        RefactoringHandler handler = null;
        IGitService gitService = new GitUtil() {
            @Override
            public boolean isCommitAnalyzed(String sha1) {
                return handler.skipCommit(sha1);
            }
        };
        Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repository, startCommitId, endCommitId);
        detect(gitService, repository, handler, walk.iterator());
    }

    private static final void detect(IGitService gitService, Repository repository, final RefactoringHandler handler, Iterator<RevCommit> i) {
        int commitsCount = 0;
        int errorCommitsCount = 0;
        int refactoringsCount = 0;

        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        String projectName = projectFolder.getName();

        long time = System.currentTimeMillis();
        while (i.hasNext()) {
            RevCommit currentCommit = i.next();
            try {
                List<Refactoring> refactoringsAtRevision = detectRefactorings(repository, handler, currentCommit);
                refactoringsCount += refactoringsAtRevision.size();

            } catch (Exception e) {
                log.warn(String.format("Ignored revision %s due to error", currentCommit.getId().getName()), e);
                handler.handleException(currentCommit.getId().getName(), e);
                errorCommitsCount++;
            }

            commitsCount++;
            long time2 = System.currentTimeMillis();
            if ((time2 - time) > 20000) {
                time = time2;
                log.info(String.format("Processing %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));
            }
        }

        handler.onFinish(refactoringsCount, commitsCount, errorCommitsCount);
        log.info(String.format("Analyzed %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));
    }

    protected static List<Refactoring> detectRefactorings(Repository repository, RefactoringHandler handler, String currentCommitId
            , String cloneURL) {
        List<Refactoring> refactoringsAtRevision = Collections.emptyList();
        try {
            File projectFolder = repository.getDirectory().getParentFile();

            List<String> filesBefore = new ArrayList<String>();
            List<String> filesCurrent = new ArrayList<String>();

            Map<String, String> renamedFilesHint = new HashMap<String, String>();
            String parentCommitId = populateWithGitHubAPI(cloneURL, currentCommitId, filesBefore, filesCurrent, renamedFilesHint);

            File currentFolder = new File(projectFolder.getParentFile(), projectFolder.getName() + "-" + currentCommitId);
            File parentFolder = new File(projectFolder.getParentFile(), projectFolder.getName() + "-" + parentCommitId);
            if (!currentFolder.exists()) {
                GitUtil.downloadProject(cloneURL, projectFolder, currentCommitId);
            }
            if (!parentFolder.exists()) {
                GitUtil.downloadProject(cloneURL, projectFolder, parentCommitId);
            }
            if (currentFolder.exists() && parentFolder.exists()) {
                UMLModel currentUMLModel = createModel(currentFolder, filesCurrent);
                UMLModel parentUMLModel = createModel(parentFolder, filesBefore);
                // Diff between currentModel e parentModel
                refactoringsAtRevision = parentUMLModel.diff(currentUMLModel, renamedFilesHint).getRefactorings();
                refactoringsAtRevision = filter(refactoringsAtRevision);
            } else {
                log.warn(String.format("Folder %s not found", currentFolder.getPath()));
            }
        } catch (Exception e) {
            log.warn(String.format("Ignored revision %s due to error", currentCommitId), e);
            handler.handleException(currentCommitId, e);
        }
        handler.handle(currentCommitId, refactoringsAtRevision);

        return refactoringsAtRevision;
    }
}
