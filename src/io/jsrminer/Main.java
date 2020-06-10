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
            //"tmp/toy_js",
            //"https://github.com/danilofes/refactoring-toy-example.git");

            String folder = "tmp";
           // String startCommit = "2c52c4265ba420ff47dc35eb1060a57c0813ee5d";
            //String endCommit = "a08feed8c410b89fa049fdbd6b9459e2d858e912";

            String startCommit = "c7b452480c5b74ccb2a3e721f487d4fcf98abfd7";
            String endCommit = "2ef9925ca949b85c421fef7cbb09131186ac0b53";
            new JSRefactoringMiner().detectBetweenCommits(repo, startCommit, endCommit);
            //JSRefactoringMiner.detectBetweenDirectories("tmp\\src1", "tmp\\src2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }
}
