package io.jsrminer;

import io.jsrminer.api.IGitService;
import io.jsrminer.io.GitUtil;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        log.debug("Program Starts");
        IGitService git = new GitUtil();
        try {
            Repository repo = git.cloneIfNotExists(
                    "tmp/refactoring-toy-example",
                    "https://github.com/danilofes/refactoring-toy-example.git");

            String folder = "tmp";
            String startCommit = "tod";
            String endCommit = "tod";

            JSRefactoringMiner.detectBetweenCommits(repo, startCommit, endCommit);
            //JSRefactoringMiner.detectBetweenDirectories("tmp\\src1", "tmp\\src2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }
}
