package io.jsrminer;

import io.jsrminer.api.IGitHistoryMiner;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.io.FileUtil;
import io.jsrminer.io.GitUtil;
import io.jsrminer.io.SourceFile;
import io.jsrminer.sourcetree.JsConfig;
import io.jsrminer.uml.UMLModel;
import io.jsrminer.uml.UMLModelFactory;
import io.jsrminer.uml.diff.SourceDirDiff;
import io.jsrminer.uml.diff.SourceDirectory;
import io.jsrminer.uml.diff.UMLModelDiff;
import io.jsrminer.uml.diff.UMLModelDiffer;
import io.jsrminer.util.RefactoringDisplayFormatter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JSRefactoringMiner implements IGitHistoryMiner {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Set<String> supportedExtensions = new HashSet<>(Arrays.asList(new String[]{JsConfig.JS_FILE_EXTENSION}));
    private final Set<String> ignoredExtensions = new HashSet<>(Arrays.asList(JsConfig.IGNORED_FILE_EXTENSIONS));

    public List<IRefactoring> detectAtCommits(String gitRepositoryPath, List<String> commitIds) {
        List<IRefactoring> refactorings = null;
        try {
            StopWatch watch = new StopWatch();
            watch.start();
            Repository repository = GitUtil.openRepository(gitRepositoryPath);
            List<RevCommit> commits = GitUtil.getRevCommits(repository, commitIds);

            var project = gitRepositoryPath.substring(gitRepositoryPath.lastIndexOf("\\") + 1,
                    gitRepositoryPath.length());

            refactorings = detect(repository, commits, new RefactoringHandler() {
                final StringBuilder builder = new StringBuilder();

                @Override
                public void handle(String commitId, List<IRefactoring> refactorings) {
                    String commitStr = RefactoringDisplayFormatter.generateDisplayStringForRefactorings(project, commitId, refactorings, false);
                    builder.append(commitStr);
                }

                @Override
                public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {

                    log.info("\n" + RefactoringDisplayFormatter.getHeader() + "\n" + builder.toString());
                }
            });

            log.info("RefCount: " + refactorings.size());
            watch.stop();
            log.info("Time taken: " + watch.toString());

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
        }
        return refactorings;
    }

    public List<IRefactoring> detectAtCommit(String gitRepositoryPath, String commitId) {
        List<IRefactoring> refactorings = null;
        try {
            StopWatch watch = new StopWatch();
            watch.start();
            Repository repository = GitUtil.openRepository(gitRepositoryPath);
            RevCommit commit = GitUtil.getRevCommit(repository, commitId);
            Iterable<RevCommit> walk = List.of(commit);
            refactorings = detect(repository, walk, null);
            log.info("RefCount: " + refactorings.size());

            printRefactorings(gitRepositoryPath.substring(gitRepositoryPath.lastIndexOf("\\") + 1,
                    gitRepositoryPath.length()), commitId, refactorings, true);

            watch.stop();
            log.info("Time taken: " + watch.toString());

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
        }
        return refactorings;
    }

    private void printRefactorings(String project, String commitId, List<IRefactoring> refactorings, boolean printHeader) {
        var str = RefactoringDisplayFormatter.generateDisplayStringForRefactorings(project, commitId, refactorings, printHeader);
        log.info("\n" + str);
    }

    @Override
    public List<IRefactoring> detectAtCurrentCommit(String gitRepositoryPath) {
        List<IRefactoring> refactorings = null;
        try {
            Repository repository = GitUtil.openRepository(gitRepositoryPath);
            RevCommit commit = GitUtil.getCurrentCommit(repository);
            return detectAtCommit(gitRepositoryPath, commit.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return refactorings;
    }

    public List<IRefactoring> detectBetweenDirectories(String previousVersionDirectory, String currentVersionDirectory) {
        SourceDirectory src1 = new SourceDirectory(previousVersionDirectory);
        SourceDirectory src2 = new SourceDirectory(currentVersionDirectory);
        SourceDirDiff diff = src1.diff(src2);

        try {
            Map<String, String> fileContentsBefore = populateFileContents(src1.getSourceFiles().values().toArray(SourceFile[]::new));
            Map<String, String> fileContentsCurrent = populateFileContents(src2.getSourceFiles().values().toArray(SourceFile[]::new));
            List<IRefactoring> refactorings = detectRefactorings(fileContentsBefore, fileContentsCurrent);
            refactorings.forEach(r -> log.info(r.toString()));
            return refactorings;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<IRefactoring> detectBetweenFiles(String filePath1, String filePath2) {
        try {
            Map<String, String> fileContentsBefore = populateFileContents(new SourceFile[]{new SourceFile(filePath1)});
            Map<String, String> fileContentsCurrent = populateFileContents(new SourceFile[]{new SourceFile(filePath2)});
            List<IRefactoring> refactorings = detectRefactorings(fileContentsBefore, fileContentsCurrent);
            refactorings.forEach(r -> log.info(r.toString()));
            return refactorings;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<IRefactoring> detectBetweenCodeSnippets(String snippetName1, String codeSnippet1,
                                                        String snippetName2, String codeSnippet2) {
        try {
            Map<String, String> fileContentsBefore = Map.of(snippetName1, codeSnippet1);
            Map<String, String> fileContentsCurrent = Map.of(snippetName2, codeSnippet2);
            List<IRefactoring> refactorings = detectRefactorings(fileContentsBefore, fileContentsCurrent);
            refactorings.forEach(r -> log.info(r.toString()));
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
            fileContents.put(files[i].getPathFromSourceDirectory(), writer.toString());
        }

        return fileContents;
    }

    public void detectBetweenCommits(Repository repository, String startCommitId, String endCommitId) throws Exception {
        Iterable<RevCommit> walk = GitUtil.createRevsWalkBetweenCommits(repository, startCommitId, endCommitId);
        detect(repository, walk, null);
    }

    private List<IRefactoring> detect(Repository repository, Iterable<RevCommit> commits, RefactoringHandler handler) {
        int commitsCount = 0;
        int errorCommitsCount = 0;
        int refactoringsCount = 0;

        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        String projectName = projectFolder.getName();

        long time = System.currentTimeMillis();
        final Iterator<RevCommit> i = commits.iterator();
        List<IRefactoring> allRefactorings = new ArrayList<>();

        if (handler == null) {
            handler = new RefactoringHandler() {
            };
        }

        while (i.hasNext()) {
            RevCommit currentCommit = i.next();
            try {
                List<IRefactoring> refactoringsAtRevision = detectRefactorings(repository, currentCommit, handler);
                refactoringsCount += refactoringsAtRevision.size();
                allRefactorings.addAll(refactoringsAtRevision);

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
        return allRefactorings;
    }

    protected List<IRefactoring> detectRefactorings(Repository repository, RevCommit currentCommit, RefactoringHandler handler) throws Exception {
        List<IRefactoring> refactoringsAtRevision = null;
        String commitId = currentCommit.getId().getName();

        List<String> filePathsBefore = new ArrayList<String>();
        List<String> filePathsCurrent = new ArrayList<String>();
        Map<String, String> renamedFilesHint = new HashMap<>();

        Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
        Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
        Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
        Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit parentCommit = walk.parseCommit(currentCommit.getParent(0).getId());
            GitUtil.fileTreeDiff(repository, parentCommit, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint
                    , supportedExtensions.toArray(String[]::new));

            // If no java files changed, there is no refactoring. Also, if there are
            // only ADD's or only REMOVE's there is no refactoring
            if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty() && currentCommit.getParentCount() > 0) {

                //Instant startTime = Instant.now();
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                // TODO Multi thread?
                log.info("Parsing and loading files of parent commit: " + parentCommit + "...");
                populateFileContents(repository, parentCommit, filePathsBefore, fileContentsBefore, repositoryDirectoriesBefore);
                UMLModel umlModelBefore = UMLModelFactory.createUMLModel(fileContentsBefore/*, repositoryDirectoriesBefore*/);

                // TODO multi thread?
                log.info("Parsing and loading files of current commit: " + parentCommit + "...");
                populateFileContents(repository, currentCommit, filePathsCurrent, fileContentsCurrent, repositoryDirectoriesCurrent);
                UMLModel umlModelCurrent = UMLModelFactory.createUMLModel(fileContentsCurrent/*, repositoryDirectoriesCurrent*/);

                stopWatch.stop();
                log.debug("Time taken for parsing and loading models: " + stopWatch.toString());

                log.info("Detecting refactorings...");
                UMLModelDiff diff = new UMLModelDiffer(umlModelBefore, umlModelCurrent).diff(renamedFilesHint);
                refactoringsAtRevision = diff.getRefactorings();
                //refactoringsAtRevision = filter(refactoringsAtRevision);
            } else {
                log.info(String.format("Ignored revision %s with no changes in js files", commitId));
                refactoringsAtRevision = Collections.emptyList();
            }
            handler.handle(commitId, refactoringsAtRevision);

            walk.dispose();
        }
        return refactoringsAtRevision;
    }

    public List<IRefactoring> detectRefactorings(Map<String, String> fileContentsBefore, Map<String, String> fileContentsCurrent) {

        // TODO Multi thread?
        UMLModel umlModelBefore = UMLModelFactory.createUMLModel(fileContentsBefore);

        // TODO multi thread?
        UMLModel umlModelCurrent = UMLModelFactory.createUMLModel(fileContentsCurrent);

        log.info("Detecting Refactorings...");
        UMLModelDiff diff = new UMLModelDiffer(umlModelBefore, umlModelCurrent).diff(new LinkedHashMap<>());

        /*, renamedFilesHint*/
        List<IRefactoring> refactorings = diff.getRefactorings();
        return refactorings;
    }

    private void populateFileContents(Repository repository, RevCommit commit,
                                      List<String> filePaths, Map<String, String> fileContents,
                                      Set<String> repositoryDirectories) throws Exception {
        //log.info("Processing {} {} ...", repository.getDirectory().getParent(), commit.getName());
        RevTree parentTree = commit.getTree();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(parentTree);
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                String pathString = treeWalk.getPathString();
                if (isExtensionAllowed(pathString) && filePaths.contains(pathString)) {
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(loader.openStream(), writer, StandardCharsets.UTF_8);
                    fileContents.put(pathString, writer.toString());
//                    var content = new String(repository.newObjectReader().open(objectId).getBytes()
//                            , StandardCharsets.UTF_8.name());
                    //fileContents.put(pathString, content);
                }
                populateSubDirectories(repositoryDirectories, pathString, '/');
            }
        }
    }

    private void populateSubDirectories(Set<String> repositoryDirectories, String pathString, char pathSeparator) {
        if (isExtensionAllowed(pathString) && pathString.indexOf(pathSeparator) != -1) {
            String directory = pathString.substring(0, pathString.lastIndexOf(pathSeparator));
            repositoryDirectories.add(directory);
            //include sub-directories
            String subDirectory = new String(directory);
            while (subDirectory.contains("/")) {
                subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf(pathSeparator));
                repositoryDirectories.add(subDirectory);
            }
        }
    }

    protected boolean isExtensionAllowed(String path) {
        String extension = FileUtil.getExtension(path).toLowerCase();
        return !path.toLowerCase().endsWith("min.js") && supportedExtensions.contains(extension);
    }
}
