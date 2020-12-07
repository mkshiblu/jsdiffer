package io.jsrminer;

import io.jsrminer.io.GitUtil;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class Main {
    //private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        log.debug("Program Starts");
        try {
            //commitExample();
            //directoryExample();
            new JSRefactoringMiner().detectAtCurrentCommit("E:\\PROJECTS_REPO\\toy_js");

        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }

    private static void commitExample() throws Exception {
        Repository repo = GitUtil.openRepository("E:\\PROJECTS_REPO\\toy_js");
        String folder = "tmp";
        String commitId = "a2d18bf1f7b7bb5e31c04d2b6f47cbbc0516b453";
        String parentCommitId = "c19d6d1d820843feada654ebd21f49a802a5be21";
        new JSRefactoringMiner().detectBetweenCommits(repo, parentCommitId, commitId);
    }

    static void directoryExample() {
        new JSRefactoringMiner().detectBetweenDirectories("tmp\\src1", "tmp\\src2");
    }
}
