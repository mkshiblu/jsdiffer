package io.jsrminer.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.nio.file.Paths;
import java.util.List;

import static org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class GitUtil {
/*
    public static void fileTreeDiff(Repository repository, RevCommit commitBefore, RevCommit commitAfter
            , List<String> filesBefore, List<String> filesAfter, String[] supportedExtensions) {
        try {
            ObjectId oldHead = commitBefore.getTree();
            ObjectId head = commitAfter.getTree();

            // prepare the two iterators to compute the diff between
            ObjectReader reader = repository.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);
            // finally get the list of changed files
            try (Git git = new Git(repository)) {
                List<DiffEntry> diffs = git.diff()
                        .setNewTree(newTreeIter)
                        .setOldTree(oldTreeIter)
                        .setShowNameAndStatusOnly(true)
                        .call();
                for (DiffEntry entry : diffs) {
                    DiffEntry.ChangeType changeType = entry.getChangeType();
                    if (changeType != ChangeType.ADD) {
                        String oldPath = entry.getOldPath();
                        if (fileExtensions.isAllowed(oldPath)) {
                            filesBefore.add(new SourceFile(Paths.get(oldPath)));
                        }
                    }
                    if (changeType != ChangeType.DELETE) {
                        String newPath = entry.getNewPath();
                        if (fileExtensions.isAllowed(newPath)) {
                            filesAfter.add(new SourceFile(Paths.get(newPath)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/
}
