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
            Repository repo = GitUtil.openRepository("E:\\PROJECTS_REPO\\toy_js");
            String startCommit = "6ec57527498d04e62c1df38d45fbb65dc63f0b43";
            String endCommit = "ce3f05c03832a7f7bd91b0b12c8bea0aa230572e";
            new JSRefactoringMiner().detectBetweenCommits(repo, startCommit, endCommit);
            //JSRefactoringMiner.detectBetweenDirectories("tmp\\src1", "tmp\\src2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }
}
