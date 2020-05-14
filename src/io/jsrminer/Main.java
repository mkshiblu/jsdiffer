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
            Repository repo = git.openRepository("F:\\PROJECTS_REPO\\toy_js");
                    //"tmp/toy_js",
                    //"https://github.com/danilofes/refactoring-toy-example.git");

            String folder = "tmp";
            String startCommit = "de83eb12c7fa8970d3952d506c5b5c3e844e9016";
            String endCommit = "b8c28a85bd7019009d15ee05ebb4011527312dbe";

            new JSRefactoringMiner().detectBetweenCommits(repo, startCommit, endCommit);
            //JSRefactoringMiner.detectBetweenDirectories("tmp\\src1", "tmp\\src2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }
}
