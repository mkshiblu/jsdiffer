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
        String repoPath = "E:\\PROJECTS_REPO\\react-native";
        String commitId = "166264d63b28582e31d083fab96ef46bc02ea469";

        Repository repo = GitUtil.openRepository(repoPath);
        String folder = "tmp";
        new JSRefactoringMiner().detectAtCommit(repoPath, commitId);
    }

    static void directoryExample() {
        new JSRefactoringMiner().detectBetweenDirectories("tmp\\s2", "tmp\\s1");
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
