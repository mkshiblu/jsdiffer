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
            String commitId = "aac0c407169dba1a393aa478e45e34de2e5b27f6";
            String parentCommitId = "e2a0d7d35cc08a6f6c9f42d289e823c3781d26f8";
            new JSRefactoringMiner().detectBetweenCommits(repo, parentCommitId, commitId);
            //JSRefactoringMiner.detectBetweenDirectories("tmp\\src1", "tmp\\src2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }
}
