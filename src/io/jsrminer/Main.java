package io.jsrminer;

import io.jsrminer.io.GitUtil;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        log.info("Program Starts");
        try {
            //webpackExample();
//            threeJsExample();
            angular();
            //  atom();
            //commitsExample();
//            vueExample();
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


    private static void angular() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\angular.js";
        String[] commitIds = new String[]{"03043839d5a540b02208001fe12e812dfde00a8e", "0cbc50512126fa22546dbe9b79a14939d9dc4459", "0ce9348bc01811a88e0e19f85e5bf74536ad4dd4", "189461f9bf6fda18ddbd16c42f2e959cf939c3da", "1e90d030771b68d55dffd56898a380f903712b66", "1e9eadcd72dbbd5c67dae8328a63e535cfa91ff9", "223de59e988dc0cc8b4ec3a045b7c0735eba1c77", "2ee5033967d5f87a516bad137686b0592e25d26b", "38f8c97af74649ce224b6dd45f433cc665acfbfb", "42e622b751d1ad1520b824473f9ad31e7efb75b3", "4a12ae7b3617e7c5389cf67ce3e3c7b309fcd9b9", "538f4606ff32e776b093243fed4af3460df74f2a", "5878f07474755cb3df1e727cef4e7e4716f44783", "60069e67aeb0352a7849ebfef15a6183bc3b7235", "667d4734fcf7a58a58d29bd87fac32a7831df184", "7e2e235cf25389a59ea957631ff07c6cc8a3ad32", "817ac56719505680ac4c9997972e8f39eb40a6d0", "837acd14e005c79d8e79f59a0075075f125da50c", "840b5f0a7634efdf4d6ed81a52e66fc0e0620127", "8d6ac5f3178cb6ead6b3b7526c50cd1c07112097", "8e104ee508418bc2ebb65e5b4ac73d22285cc224", "9d74f0fdcb44aa597b81a2bde967d4c37b60dce2", "b0f6afcdacc53fd91b37ad0c5d70544f7e37ebde", "b7bb797e5c7bdc93f2ffca39baf6e112e6dddbe2", "b7d396b8b6e8f27a1f4556d58fc903321e8d532a", "bf5c2eef34a314d5d2298fdcb6deaa7282cc563f", "cdaa6a951b8fce67673bec224a7ad1f24f0a05bf", "de74034ddf6f92505ccdb61be413a6df2c723f87", "e3ece2fad9e1e6d47b5f06815ff186d7e6f44948", "fd4f0111188b62773b99ab6eab38b4d2b5d8d727", "ff0e61166d3dca59351e3913e0360c24d1bce99c"
        };
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }

    private static void atom() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\atom";
        String[] commitIds = new String[]{/*"062dfdea80861f9c918c99a3f86895f908635862", "113453a219e692fe1e5f0eb3c1c09703daaa0a89", "1cdb804824546e8572912df8cf51c9d5072c33ba",*/ "48d36776648149c4170fed630207ef9ec1cb962a", "4dbfadd0d5a603fec292519c9115c857e26201eb", "72c30d7e78f94e432874b54e2c9d71857a06211a", "7ce5b000e448552bb4ba9556c8f38ccfef127162", "adbd5400295494173e59285bbcb6ac967ed3ebfb", "bf6a4e3db4b28bfe068feb72199746dad98d6a83", "cb783fd15cea1d06b254799e7af8ff71d7d7ed33", "e2c480847c81f6f97cc184cb5353080ffbcd590d", "f6d2d5729944abbff8a2db1ab2a35d223e998b8c"};
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }

    private static void reactNativeExample() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\react-native";
        String[] commitIds = new String[]{"0125813f213662ec1d9bb4a456f5671adcff9d83", "0125813f213662ec1d9bb4a456f5671adcff9d83", "06c05e744d8af9582bde348210f254d76dae48b9", "2f4ca831bfd936be9ae71a8a5700003abb65a0f5", "4435f087713e1d0ac3639e3b3196d71c6402898e", "488a4c7e1c86ac5900ff9194106511fbf5a8e3cb", "5259450c143f71c65e157d6b7d3f0e1655eb7aa1", "7014a30baae0b01378740113d15e4a7d721d916a", "83169c95a3525fcb74e34853022475093b9a1a77", "95e8592dcb9b50772336d2801f46305fdc95250d", "970caa4552d4ba87c1a954391535ff42b00832e7", "9fbff62b5ff9b3effb18f135e1cd9bd34a83c985", "a90d0e3614c467c33cf85bcbe65be71903d5aecc", "ad67f556fb145d38fe547e1e8baa73ccc7a431b0", "b549e364e0025e0e1b4005f04a9de2d767006da1", "c749d951ada829c6f6fb76f35e68142e61054433", "d1336ab16efc26449509b938dd46cf606d3caf34", "e708010d18f938e2d6b6424cfc9485d8e5dd2800", "f59e5a8d28491c64e4d0c08fec3a2b0b3fabb38b", "f6da9e1a9a932d2775cc998469d2f9b4ebd858c5", "f80000b9e72596392567a3deda59441e43f6327d", "f8c8231706492b588331354d45b833aa21434e13", "fbd1beaf666be9c09a380784f8c0cd34ba083a6b"};
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }

    private static void webpackExample() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\webpack";
        String commitId = "53103a9690d653daf1de405756f5638999c36f22";
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

    static void threeJsExample() throws IOException {
        String repoPath = "E:\\PROJECTS_REPO\\three.js";
        String commitId = "01b263d3362f8bab63dc26b04e4a7ac1fb1d2641";
        new JSRefactoringMiner().detectAtCommit(repoPath, commitId);
    }
}
