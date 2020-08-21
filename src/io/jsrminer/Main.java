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
            String startCommit = "9fa37a104973ca762d2dc90e30ef00d28b81a649";
            String endCommit = "2b48feaa0b8caa027c28592b8696de347d86d3fd";
            new JSRefactoringMiner().detectBetweenCommits(repo, startCommit, endCommit);
            //JSRefactoringMiner.detectBetweenDirectories("tmp\\src1", "tmp\\src2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }
}
