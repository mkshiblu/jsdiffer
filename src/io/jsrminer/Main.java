package io.jsrminer;

import io.jsrminer.io.GitUtil;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        log.debug("Program Starts");
        try {
            Repository repo = GitUtil.openRepository("F:\\PROJECTS_REPO\\toy_js");
            String folder = "tmp";
            String commitId = "8b0dada1caca6b3d5bb5a2b39c9ebbf9103885b9";
            String parentCommitId = "b47d89d8c20a698f02cfa1281c6e8013b99500fc";
            new JSRefactoringMiner().detectBetweenCommits(repo, parentCommitId, commitId);
            //JSRefactoringMiner.detectBetweenDirectories("tmp\\src1", "tmp\\src2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }
}
