package io.jsrminer.io;

import io.jsrminer.api.Churn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GitUtil {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static final String REMOTE_REFS_PREFIX = "refs/remotes/origin/";
    private static final String GITHUB_URL = "https://github.com/";
    private static final String BITBUCKET_URL = "https://bitbucket.org/";

    GitUtil.DefaultCommitsFilter commitsFilter = new GitUtil.DefaultCommitsFilter();

    public Repository cloneIfNotExists(String projectPath, String cloneUrl/*, String branch*/) throws Exception {
        File folder = new File(projectPath);
        Repository repository;
        if (folder.exists()) {
            RepositoryBuilder builder = new RepositoryBuilder();
            repository = builder
                    .setGitDir(new File(folder, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            log.info("Project {} is already cloned, current branch is {}", cloneUrl, repository.getBranch());

        } else {
            log.info("Cloning {} ...", cloneUrl);
            Git git = Git.cloneRepository()
                    .setDirectory(folder)
                    .setURI(cloneUrl)
                    .setCloneAllBranches(true)
                    .call();
            repository = git.getRepository();
            log.info("Done cloning {}, current branch is {}", cloneUrl, repository.getBranch());
        }

//		if (branch != null && !repository.getBranch().equals(branch)) {
//			Git git = new Git(repository);
//
//			String localBranch = "refs/heads/" + branch;
//			List<Ref> refs = git.branchList().call();
//			boolean branchExists = false;
//			for (Ref ref : refs) {
//				if (ref.getName().equals(localBranch)) {
//					branchExists = true;
//				}
//			}
//
//			if (branchExists) {
//				git.checkout()
//					.setName(branch)
//					.call();
//			} else {
//				git.checkout()
//					.setCreateBranch(true)
//					.setName(branch)
//					.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
//					.setStartPoint("origin/" + branch)
//					.call();
//			}
//
//			logger.info("Project {} switched to {}", cloneUrl, repository.getBranch());
//		}
        return repository;
    }

    public Repository openRepository(String repositoryPath) throws Exception {
        File folder = new File(repositoryPath);
        Repository repository;
        if (folder.exists()) {
            RepositoryBuilder builder = new RepositoryBuilder();
            repository = builder
                    .setGitDir(new File(folder, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } else {
            throw new FileNotFoundException(repositoryPath);
        }
        return repository;
    }

    public void checkout(Repository repository, String commitId) throws Exception {
        log.info("Checking out {} {} ...", repository.getDirectory().getParent().toString(), commitId);
        try (Git git = new Git(repository)) {
            CheckoutCommand checkout = git.checkout().setName(commitId);
            checkout.call();
        }
//		File workingDir = repository.getDirectory().getParentFile();
//		ExternalProcess.execute(workingDir, "git", "checkout", commitId);
    }

    public void checkout2(Repository repository, String commitId) throws Exception {
        log.info("Checking out {} {} ...", repository.getDirectory().getParent().toString(), commitId);
        File workingDir = repository.getDirectory().getParentFile();
        String output = ExternalProcess.execute(workingDir, "git", "checkout", commitId);
        if (output.startsWith("fatal")) {
            throw new RuntimeException("git error " + output);
        }
    }

    public int countCommits(Repository repository, String branch) throws Exception {
        RevWalk walk = new RevWalk(repository);
        try {
            Ref ref = repository.findRef(REMOTE_REFS_PREFIX + branch);
            ObjectId objectId = ref.getObjectId();
            RevCommit start = walk.parseCommit(objectId);
            walk.setRevFilter(RevFilter.NO_MERGES);
            return RevWalkUtils.count(walk, start, null);
        } finally {
            walk.dispose();
        }
    }

    private List<TrackingRefUpdate> fetch(Repository repository) throws Exception {
        log.info("Fetching changes of repository {}", repository.getDirectory().toString());
        try (Git git = new Git(repository)) {
            FetchResult result = git.fetch().call();

            Collection<TrackingRefUpdate> updates = result.getTrackingRefUpdates();
            List<TrackingRefUpdate> remoteRefsChanges = new ArrayList<TrackingRefUpdate>();
            for (TrackingRefUpdate update : updates) {
                String refName = update.getLocalName();
                if (refName.startsWith(REMOTE_REFS_PREFIX)) {
                    ObjectId newObjectId = update.getNewObjectId();
                    log.info("{} is now at {}", refName, newObjectId.getName());
                    remoteRefsChanges.add(update);
                }
            }
            if (updates.isEmpty()) {
                log.info("Nothing changed");
            }
            return remoteRefsChanges;
        }
    }

    public RevWalk fetchAndCreateNewRevsWalk(Repository repository) throws Exception {
        return this.fetchAndCreateNewRevsWalk(repository, null);
    }

    public RevWalk fetchAndCreateNewRevsWalk(Repository repository, String branch) throws Exception {
        List<ObjectId> currentRemoteRefs = new ArrayList<ObjectId>();
        for (Ref ref : repository.getRefDatabase().getRefs()) {
            String refName = ref.getName();
            if (refName.startsWith(REMOTE_REFS_PREFIX)) {
                currentRemoteRefs.add(ref.getObjectId());
            }
        }

        List<TrackingRefUpdate> newRemoteRefs = this.fetch(repository);

        RevWalk walk = new RevWalk(repository);
        for (TrackingRefUpdate newRef : newRemoteRefs) {
            if (branch == null || newRef.getLocalName().endsWith("/" + branch)) {
                walk.markStart(walk.parseCommit(newRef.getNewObjectId()));
            }
        }
        for (ObjectId oldRef : currentRemoteRefs) {
            walk.markUninteresting(walk.parseCommit(oldRef));
        }
        walk.setRevFilter(commitsFilter);
        return walk;
    }

    public RevWalk createAllRevsWalk(Repository repository) throws Exception {
        return this.createAllRevsWalk(repository, null);
    }

    public RevWalk createAllRevsWalk(Repository repository, String branch) throws Exception {
        List<ObjectId> currentRemoteRefs = new ArrayList<ObjectId>();
        for (Ref ref : repository.getRefDatabase().getRefs()) {
            String refName = ref.getName();
            if (refName.startsWith(REMOTE_REFS_PREFIX)) {
                if (branch == null || refName.endsWith("/" + branch)) {
                    currentRemoteRefs.add(ref.getObjectId());
                }
            }
        }

        RevWalk walk = new RevWalk(repository);
        for (ObjectId newRef : currentRemoteRefs) {
            walk.markStart(walk.parseCommit(newRef));
        }
        walk.setRevFilter(commitsFilter);
        return walk;
    }

    public Iterable<RevCommit> createRevsWalkBetweenTags(Repository repository, String startTag, String endTag)
            throws Exception {
        Ref refFrom = repository.findRef(startTag);
        Ref refTo = repository.findRef(endTag);
        try (Git git = new Git(repository)) {
            List<RevCommit> revCommits = StreamSupport.stream(git.log().addRange(getActualRefObjectId(refFrom), getActualRefObjectId(refTo)).call()
                    .spliterator(), false)
                    .filter(r -> r.getParentCount() == 1)
                    .collect(Collectors.toList());
            Collections.reverse(revCommits);
            return revCommits;
        }
    }

    private ObjectId getActualRefObjectId(Ref ref) {
        if (ref.getPeeledObjectId() != null) {
            return ref.getPeeledObjectId();
        }
        return ref.getObjectId();
    }

    public static Iterable<RevCommit> createRevsWalkBetweenCommits(Repository repository, String startCommitId, String endCommitId)
            throws Exception {
        ObjectId from = repository.resolve(startCommitId);
        ObjectId to = repository.resolve(endCommitId);
        try (Git git = new Git(repository)) {
            List<RevCommit> revCommits = StreamSupport.stream(git.log().addRange(from, to).call()
                    .spliterator(), false)
                    .filter(r -> r.getParentCount() == 1)
                    .collect(Collectors.toList());
            Collections.reverse(revCommits);
            return revCommits;
        }
    }

    public boolean isCommitAnalyzed(String sha1) {
        return false;
    }

    private class DefaultCommitsFilter extends RevFilter {
        @Override
        public final boolean include(final RevWalk walker, final RevCommit c) {
            return c.getParentCount() == 1 && !isCommitAnalyzed(c.getName());
        }

        @Override
        public final RevFilter clone() {
            return this;
        }

        @Override
        public final boolean requiresCommitBody() {
            return false;
        }

        @Override
        public String toString() {
            return "RegularCommitsFilter";
        }
    }
//
//
//    /**
//     * Finds the of two commits
//     */
//    public static void fileTreeDiff(final Repository repository, final RevCommit commitBefore
//            , final RevCommit commitAfter
//            , final List<String> filesBefore, final List<String> filesAfter, final String[] supportedExtensions) {
//        try {
//            final ObjectId oldHead = commitBefore.getTree();
//            final ObjectId head = commitAfter.getTree();
//
//            final Set<String> allowedExtensionsSet = Arrays.stream(supportedExtensions)
//                    .map(extension -> extension.toLowerCase())
//                    .collect(Collectors.toSet());
//
//            // prepare the two iterators to compute the diff between
//            ObjectReader reader = repository.newObjectReader();
//            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
//            oldTreeIter.reset(reader, oldHead);
//            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
//            newTreeIter.reset(reader, head);
//
//            // finally get the list of changed files
//            try (Git git = new Git(repository)) {
//                List<DiffEntry> diffs = git.diff()
//                        .setNewTree(newTreeIter)
//                        .setOldTree(oldTreeIter)
//                        .setShowNameAndStatusOnly(true)
//                        .call();
//                for (DiffEntry entry : diffs) {
//                    DiffEntry.ChangeType changeType = entry.getChangeType();
//                    if (changeType != DiffEntry.ChangeType.ADD) {
//                        String oldPath = entry.getOldPath();
//                        if (allowedExtensionsSet.contains(FileUtil.getExtension(oldPath).toLowerCase()))
//                            filesBefore.add(Paths.get(oldPath).toString());
//                    }
//                    if (changeType != DiffEntry.ChangeType.DELETE) {
//                        String newPath = entry.getNewPath();
//                        if (allowedExtensionsSet.contains(FileUtil.getExtension(newPath).toLowerCase())) {
//                            filesAfter.add(Paths.get(newPath).toString());
//
//                            if (changeType == DiffEntry.ChangeType.RENAME) {
//                                String oldPath = entry.getOldPath();
//                                renamedFilesHint.put(oldPath, newPath);
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * Finds the file differences between two commits. The parameters are
     */
    public static void fileTreeDiff(Repository repository, RevCommit commitBefore, RevCommit commitAfter, List<String> filesBefore,
                                    List<String> filesAfter, Map<String, String> renamedFilesHint, String[] allowedFileExtensions) throws Exception {

        ObjectId oldTree = commitBefore.getTree();
        ObjectId newTree = commitAfter.getTree();

        final Set<String> allowedExtensionsSet = Arrays.stream(allowedFileExtensions)
                .map(extension -> extension.toLowerCase())
                .collect(Collectors.toSet());

        final TreeWalk tw = new TreeWalk(repository);
        tw.setRecursive(true);
        tw.addTree(oldTree);
        tw.addTree(newTree);

        final RenameDetector rd = new RenameDetector(repository);
        rd.setRenameScore(80);
        rd.addAll(DiffEntry.scan(tw));

        for (DiffEntry diff : rd.compute(tw.getObjectReader(), null)) {
            DiffEntry.ChangeType changeType = diff.getChangeType();
            String oldPath = diff.getOldPath();
            String newPath = diff.getNewPath();

            if (changeType != DiffEntry.ChangeType.ADD) {
                if (isFileExtensionAllowed(allowedExtensionsSet, oldPath)) {
                    filesBefore.add(oldPath);
                }
            }
            if (changeType != DiffEntry.ChangeType.DELETE) {
                if (isFileExtensionAllowed(allowedExtensionsSet, newPath)) {
                    filesAfter.add(newPath);
                }
            }
            if (changeType == DiffEntry.ChangeType.RENAME && diff.getScore() >= rd.getRenameScore()) {
                if (isFileExtensionAllowed(allowedExtensionsSet, oldPath) && isFileExtensionAllowed(allowedExtensionsSet, newPath)) {
                    renamedFilesHint.put(oldPath, newPath);
                }
            }
        }
    }

    private static boolean isFileExtensionAllowed(Set<String> allowedExtensionsSet, String filePath) {
        if (allowedExtensionsSet == null)
            return true;
        return allowedExtensionsSet.contains(FileUtil.getExtension(filePath).toLowerCase());
    }

    public Churn churn(Repository repository, RevCommit currentCommit) throws Exception {
        if (currentCommit.getParentCount() > 0) {
            ObjectId oldTree = currentCommit.getParent(0).getTree();
            ObjectId newTree = currentCommit.getTree();
            final TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(true);
            tw.addTree(oldTree);
            tw.addTree(newTree);

            List<DiffEntry> diffs = DiffEntry.scan(tw);
            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(repository);
            diffFormatter.setContext(0);

            int addedLines = 0;
            int deletedLines = 0;
            for (DiffEntry entry : diffs) {
                FileHeader header = diffFormatter.toFileHeader(entry);
                List<? extends HunkHeader> hunks = header.getHunks();
                for (HunkHeader hunkHeader : hunks) {
                    for (Edit edit : hunkHeader.toEditList()) {
                        if (edit.getType() == Edit.Type.INSERT) {
                            addedLines += edit.getLengthB();
                        } else if (edit.getType() == Edit.Type.DELETE) {
                            deletedLines += edit.getLengthA();
                        } else if (edit.getType() == Edit.Type.REPLACE) {
                            deletedLines += edit.getLengthA();
                            addedLines += edit.getLengthB();
                        }
                    }
                }
            }
            diffFormatter.close();
            return new Churn(addedLines, deletedLines);
        }
        return null;
    }

    /**
     * Downloads the repo as zip and extract it on the specified folder. TODO use NULL
     */
    public static final void downloadProject(String cloneURL, File destinationFolder, String commitId)
            throws IOException {
        String downloadLink = extractDownloadLink(cloneURL, commitId);
        File destinationFile = new File(destinationFolder.getParentFile(), destinationFolder.getName() + "-" + commitId + ".zip");
        log.info(String.format("Downloading archive %s", downloadLink));
        FileUtil.download(new URL(downloadLink), destinationFile);

        log.info(String.format("Unzipping archive %s", downloadLink));
        FileUtil.unZip(destinationFile);

        java.util.zip.ZipFile zipFile = new ZipFile(destinationFile);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(destinationFolder.getParentFile(), entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    in.close();
                    out.close();
                }
            }
        } finally {
            zipFile.close();
        }
    }

    /**
     * Create the download link for the the source repo as zip. Currently supports Github and BitBucket?
     */
    public static String extractDownloadLink(String cloneURL, String commitId) {
        int indexOfDotGit = cloneURL.length();
        if (cloneURL.endsWith(".git")) {
            indexOfDotGit = cloneURL.indexOf(".git");
        } else if (cloneURL.endsWith("/")) {
            indexOfDotGit = cloneURL.length() - 1;
        }
        String downloadResource = "/";
        if (cloneURL.startsWith(GITHUB_URL)) {
            downloadResource = "/archive/";
        } else if (cloneURL.startsWith(BITBUCKET_URL)) {
            downloadResource = "/get/";
        }
        String downloadLink = cloneURL.substring(0, indexOfDotGit) + downloadResource + commitId + ".zip";
        return downloadLink;
    }
}
