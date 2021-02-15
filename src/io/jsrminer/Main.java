package io.jsrminer;

import io.jsrminer.io.GitUtil;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        log.info("Program Starts");
        try {
     //       vueExample();
     reactNativeExample();
//            socketIoExample();
//            commitExample();
    //        directoryExample();
            //fileExample();
//              chartJsExample();
            //jqueryExample();

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

    private static void reactNativeExample() throws Exception{
        String repoPath = "E:\\PROJECTS_REPO\\react-native";
        String commitId = "166264d63b28582e31d083fab96ef46bc02ea469";
        new JSRefactoringMiner().detectAtCommit(repoPath, commitId);
    }
    private static void chartJsExample() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\chart-js";
        String commitId = "35dcfe00b1ae7199f8ed6c3748a72f4700c9876d";

        Repository repo = GitUtil.openRepository(repoPath);
        String folder = "tmp";
        new JSRefactoringMiner().detectAtCommit(repoPath, commitId);
    }

    static void fileExample() {
        new JSRefactoringMiner().detectBetweenFiles("tmp\\s1\\f1.js", "tmp\\s2\\f1.js");
    }
    static void directoryExample() {
        new JSRefactoringMiner().detectBetweenDirectories("tmp\\s1", "tmp\\s2");
    }

    static void jqueryExample() {
        String root = "resources\\real-projects\\jquery";
        new JSRefactoringMiner().detectBetweenDirectories(root, root);
    }

    static void vueExample() throws IOException {
        String repoPath = "E:\\PROJECTS_REPO\\vue";
        String commitId = "9bded22a83b6fb9a89a32009e7f47f6201e167a3";
        new JSRefactoringMiner().detectAtCommit(repoPath, commitId);
    }

    static void vueRuntimeExample() throws IOException {
        new JSRefactoringMiner().detectBetweenFiles("tmp\\s1\\vue_run_removed.js", "tmp\\s2\\vue_run_removed.js");
    }

    static void socketIoExample() throws IOException {
        String repoPath = "E:\\PROJECTS_REPO\\socket.io";
        String commitId = "3aa3213b13e914a668a76bc5eab1cef80708bf01";
        new JSRefactoringMiner().detectAtCommit(repoPath, commitId);
    }
}
