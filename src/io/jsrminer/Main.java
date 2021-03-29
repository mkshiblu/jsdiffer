package io.jsrminer;

import io.jsrminer.io.GitUtil;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        log.info("Program Starts");
        try {
            angualJsExample();
            //commitsExample();
         //   vueExample();
//     reactNativeExample();
//            socketIoExample();
//            commitExample();
//            directoryExample();
//            fileExample();
//              chartJsExample();
//            jqueryExample();

            //new JSRefactoringMiner().detectAtCurrentCommit("E:\\PROJECTS_REPO\\toy_js");

        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }

    private static void commitsExample() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\vue";
        var commitIds = new String[]{
                "015a31890250e93564959de97372be7bd6195f62",
//                "0c11aa8addf5ad852e37da358ce2887af72e4193",
//                "144a4dd860b20ca48263bac150286f627e08d953",
//                "2b3e1a0a964c21d405f67479a9960c2d00f235b4",
//                "2b5c83af6d8b15510424af4877d58c261ea02e16",
//                "2e0f6d5d817957ab23819f90b264243d87ef968c",
//                "3b32652aa68a06d881e3149bb21ac8711887e7f6",
//                "41d774d112946f986bf0b0e3f30fd962c01ceba2",
//                "46c8016562c3a9308fcf45d31a5a674312e2c110",
//                "4e0c48511d49f331fde31fc87b6ca428330f32d1",
//                "50b711af43708426e63b4ea529436b49fafc3f2e",
//                "55a719c93aeffb8176fcc7193aa3a813cac3099d",
//                "5db86b4e94857fdde3ae6b71e24da637bc116baa",
//                "60da366a2653a3984d79331d02ebb2ecf7e73a9a",
//                "62e47c9eb4446da79d66ad2385c199f31b4348d8",
//                "644274cbd34e14e74e8931fa979b22dc2db04895",
//                "653aac2c57d15f0e93a2c1cc7e6fad156658df19",
//                "6dac3dbe441302cebb945b675f78f8e7247e2a97",
//                "711aaf71bb126fb5a0dd3ab032a7793a74918f02",
//                "7ad368ebb6987bd4044f9df184d73ce14ca680f2",
//                "8335217cb4bd13fb07e08a76c07df0fceed6c197",
//                "88f3889f19678981944339be8d22c3ebcd11f822",
//                "8b893c13d6ffa79f294fec76a228509ec48e4706",
//                "90891709708e8ebdc1522eed678a49eb9f312fda",
//                "90ed48224e0ae281a2579b997e4bd5a150b80413",
//                "984927a1a98d10ad8af44f2accfb08d34d517610",
//                "9bded22a83b6fb9a89a32009e7f47f6201e167a3",
//                "9edcc6b6c7612efc15e4bfc5079279533190a2f2",

        };
        String commitId = "166264d63b28582e31d083fab96ef46bc02ea469";

        Repository repo = GitUtil.openRepository(repoPath);
        String folder = "tmp";
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }

    private static void angualJsExample() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\angular";
        String[] commitIds = new String[]{
                "03043839d5a540b02208001fe12e812dfde00a8e"
                , "05fdf918d97a379634c8b0e15da8615edfc6fb8d"
                , "0cbc50512126fa22546dbe9b79a14939d9dc4459"
                , "0ce9348bc01811a88e0e19f85e5bf74536ad4dd4"
                , "189461f9bf6fda18ddbd16c42f2e959cf939c3da"
                , "1e90d030771b68d55dffd56898a380f903712b66"
                , "1e9eadcd72dbbd5c67dae8328a63e535cfa91ff9"
                , "223de59e988dc0cc8b4ec3a045b7c0735eba1c77"
                , "2ee5033967d5f87a516bad137686b0592e25d26b"

        };
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }

    private static void reactNativeExample() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\react-native";
        String commitId = "e708010d18f938e2d6b6424cfc9485d8e5dd2800";
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
        new JSRefactoringMiner().detectBetweenFiles("tmp\\s1\\f1.js", "tmp\\s2\\f1.js"
        );
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
        String commitId = "2b3e1a0a964c21d405f67479a9960c2d00f235b4";
        var refs = new JSRefactoringMiner().detectAtCommit(repoPath, commitId);
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
