package io.jsrminer;

import io.jsrminer.io.GitUtil;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        log.info("Program Starts");
        try {
            //commitExample();
            directoryExample();
            //jqueryExample();
//            vueExample();
            //new JSRefactoringMiner().detectAtCurrentCommit("E:\\PROJECTS_REPO\\toy_js");

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
        new JSRefactoringMiner().detectBetweenDirectories("tmp\\vue1", "tmp\\vue2");
        new JSRefactoringMiner().detectBetweenDirectories("tmp\\vue1", "tmp\\vue2");
    }

    static void jqueryExample() {
        String root = "resources\\real-projects\\jquery";
        new JSRefactoringMiner().detectBetweenDirectories(root, root);
    }

    static void vueExample() {
        String root = "resources\\real-projects\\vue";
        new JSRefactoringMiner().detectBetweenDirectories(root + "\\src1", root + "\\src2");
    }
}
