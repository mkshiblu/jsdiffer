package io.jsrminer;

import io.jsrminer.api.IGitHistoryMiner;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.io.FileUtil;
import io.jsrminer.io.GitUtil;
import io.jsrminer.io.SourceFile;
import io.jsrminer.uml.UMLModel;
import io.jsrminer.uml.UMLModelFactory;
import io.jsrminer.uml.diff.SourceDirDiff;
import io.jsrminer.uml.diff.SourceDirectory;
import io.jsrminer.uml.diff.UMLModelDiff;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.*;

public class JSRefactoringMiner implements IGitHistoryMiner {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Set<String> supportedExtensions = new HashSet<>(Arrays.asList(new String[]{"js", "ts"}));

    @Override
    public List<IRefactoring> detectAtCurrentCommit(String gitRepositoryPath) {
        List<IRefactoring> refactorings = null;
        try {
            Repository repository = GitUtil.openRepository(gitRepositoryPath);
            RevCommit commit = GitUtil.getCurrentCommit(repository);
            Iterable<RevCommit> walk = List.of(commit);
            refactorings = detect(repository, null, walk);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return refactorings;
    }

    public List<IRefactoring> detectBetweenDirectories(String previousVersionDirectory, String currentVersionDirectory) {
        SourceDirectory src1 = new SourceDirectory(previousVersionDirectory);
        SourceDirectory src2 = new SourceDirectory(currentVersionDirectory);
        SourceDirDiff diff = src1.diff(src2);

//        System.out.println("Common Files: " + Arrays.deepToString(diff.getCommonSourceFiles()));
//        System.out.println("\nADDED: " + Arrays.deepToString(diff.getAddedFiles()));
//        System.out.println("\nDeleted: " + Arrays.deepToString(diff.getDeletedFiles()));

        try {
            Map<String, String> fileContentsBefore = populateFileContents(src1.getSourceFiles().values().toArray(SourceFile[]::new));
            Map<String, String> fileContentsCurrent = populateFileContents(src2.getSourceFiles().values().toArray(SourceFile[]::new));
            List<IRefactoring> refactorings = detectRefactorings(fileContentsBefore, fileContentsCurrent);
            refactorings.forEach(r -> System.out.println(r));
            return refactorings;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Map<String, String> populateFileContents(SourceFile[] files) throws IOException {
        Map<String, String> fileContents = new LinkedHashMap<>();
        for (int i = 0; i < files.length; i++) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(new FileInputStream(files[i].getFile()), writer, Charset.defaultCharset());
            fileContents.put(files[i].getRelativePathToSourceDirectory(), writer.toString());
        }

        return fileContents;
    }

    public void detectBetweenCommits(Repository repository, String startCommitId, String endCommitId) throws Exception {
        RefactoringHandler handler = null;
        Iterable<RevCommit> walk = GitUtil.createRevsWalkBetweenCommits(repository, startCommitId, endCommitId);
        detect(repository, handler, walk);
    }

    private List<IRefactoring> detect(Repository repository, final RefactoringHandler handler, Iterable<RevCommit> commits) {
        int commitsCount = 0;
        int errorCommitsCount = 0;
        int refactoringsCount = 0;

        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        String projectName = projectFolder.getName();

        long time = System.currentTimeMillis();
        final Iterator<RevCommit> i = commits.iterator();
        List<IRefactoring> allRefactorings = new ArrayList<>();

        while (i.hasNext()) {
            RevCommit currentCommit = i.next();
            try {
                List<IRefactoring> refactoringsAtRevision = detectRefactorings(repository, currentCommit, handler);
                refactoringsCount += refactoringsAtRevision.size();
                allRefactorings.addAll(refactoringsAtRevision);

            } catch (Exception e) {
                log.warn(String.format("Ignored revision %s due to error", currentCommit.getId().getName()), e);
                //  handler.handleException(currentCommit.getId().getName(), e);
                errorCommitsCount++;
            }

            commitsCount++;
            long time2 = System.currentTimeMillis();
            if ((time2 - time) > 20000) {
                time = time2;
                log.info(String.format("Processing %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));
            }
        }

        // handler.onFinish(refactoringsCount, commitsCount, errorCommitsCount);
        log.info(String.format("Analyzed %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));
        return allRefactorings;
    }

    protected List<IRefactoring> detectRefactorings(Repository repository, RevCommit currentCommit, RefactoringHandler handler) throws Exception {
        List<IRefactoring> refactoringsAtRevision = null;
        String commitId = currentCommit.getId().getName();

        List<String> filePathsBefore = new ArrayList<String>();
        List<String> filePathsCurrent = new ArrayList<String>();
        Map<String, String> renamedFilesHint = new HashMap<String, String>();

        Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
        Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
        Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
        Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit parentCommit = walk.parseCommit(currentCommit.getParent(0).getId());
            GitUtil.fileTreeDiff2(repository, parentCommit, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint
                    , supportedExtensions.toArray(String[]::new));

            // If no java files changed, there is no refactoring. Also, if there are
            // only ADD's or only REMOVE's there is no refactoring
            if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty() && currentCommit.getParentCount() > 0) {

                // TODO Multi thread?
                populateFileContents(repository, parentCommit, filePathsBefore, fileContentsBefore, repositoryDirectoriesBefore);
                UMLModel umlModelBefore = UMLModelFactory.createUMLModel(fileContentsBefore/*, repositoryDirectoriesBefore*/);

                // TODO multi thread?
                populateFileContents(repository, currentCommit, filePathsCurrent, fileContentsCurrent, repositoryDirectoriesCurrent);
                UMLModel umlModelCurrent = UMLModelFactory.createUMLModel(fileContentsCurrent/*, repositoryDirectoriesCurrent*/);

                UMLModelDiff diff = umlModelBefore.diff(umlModelCurrent);

                refactoringsAtRevision = umlModelBefore.diff(umlModelCurrent/*, renamedFilesHint*/).getRefactorings();
                //refactoringsAtRevision = filter(refactoringsAtRevision);
            } else {
                //logger.info(String.format("Ignored revision %s with no changes in java files", commitId));
                refactoringsAtRevision = Collections.emptyList();
            }
            //handler.handle(commitId, refactoringsAtRevision);

            walk.dispose();
        }
        return refactoringsAtRevision;
    }

    public List<IRefactoring> detectRefactorings(Map<String, String> fileContentsBefore, Map<String, String> fileContentsCurrent) {

        // TODO Multi thread?
        UMLModel umlModelBefore = UMLModelFactory.createUMLModel(fileContentsBefore);

        // TODO multi thread?
        UMLModel umlModelCurrent = UMLModelFactory.createUMLModel(fileContentsCurrent);

        UMLModelDiff diff = umlModelBefore.diff(umlModelCurrent);

        /*, renamedFilesHint*/
        List<IRefactoring> refactorings = diff.getRefactorings();
        return refactorings;
    }

    private void populateFileContents(Repository repository, RevCommit commit,
                                      List<String> filePaths, Map<String, String> fileContents,
                                      Set<String> repositoryDirectories) throws Exception {
        log.info("Processing {} {} ...", repository.getDirectory().getParent(), commit.getName());
        RevTree parentTree = commit.getTree();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(parentTree);
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                String pathString = treeWalk.getPathString();
                if (filePaths.contains(pathString)) {
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(loader.openStream(), writer, Charset.defaultCharset());
                    fileContents.put(pathString, writer.toString());
                }
                if (isExtensionAllowed(pathString) && pathString.contains("/")) {
                    String directory = pathString.substring(0, pathString.lastIndexOf("/"));
                    repositoryDirectories.add(directory);
                    //include sub-directories
                    String subDirectory = new String(directory);
                    while (subDirectory.contains("/")) {
                        subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
                        repositoryDirectories.add(subDirectory);
                    }
                }
            }
        }
    }

    protected boolean isExtensionAllowed(String path) {
        return supportedExtensions.contains(FileUtil.getExtension(path).toLowerCase());
    }
}
