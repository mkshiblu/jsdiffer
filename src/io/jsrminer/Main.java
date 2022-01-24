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
      //  angular();
//            atom();
//           axios();
          //  chartJsExample();
            createReactApp();
            //webpackExample();
//            threeJsExample();
            //commitsExample();
//            vueExample();
//     reactNativeExample();
//            socketIoExample();
//            directoryExample();
//            fileExample();
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
        String[] commitIds = new String[]{
                "03043839d5a540b02208001fe12e812dfde00a8e", "05fdf918d97a379634c8b0e15da8615edfc6fb8d", "0cbc50512126fa22546dbe9b79a14939d9dc4459", "0ce9348bc01811a88e0e19f85e5bf74536ad4dd4", "189461f9bf6fda18ddbd16c42f2e959cf939c3da", "1e90d030771b68d55dffd56898a380f903712b66", "1e9eadcd72dbbd5c67dae8328a63e535cfa91ff9", "223de59e988dc0cc8b4ec3a045b7c0735eba1c77", "2ee5033967d5f87a516bad137686b0592e25d26b", "38f8c97af74649ce224b6dd45f433cc665acfbfb", "42e622b751d1ad1520b824473f9ad31e7efb75b3", "4a12ae7b3617e7c5389cf67ce3e3c7b309fcd9b9", "538f4606ff32e776b093243fed4af3460df74f2a", "5878f07474755cb3df1e727cef4e7e4716f44783", "60069e67aeb0352a7849ebfef15a6183bc3b7235", "667d4734fcf7a58a58d29bd87fac32a7831df184", "7e2e235cf25389a59ea957631ff07c6cc8a3ad32", "817ac56719505680ac4c9997972e8f39eb40a6d0", "837acd14e005c79d8e79f59a0075075f125da50c", "840b5f0a7634efdf4d6ed81a52e66fc0e0620127", "8d6ac5f3178cb6ead6b3b7526c50cd1c07112097",
                "8e104ee508418bc2ebb65e5b4ac73d22285cc224"
                , "9d74f0fdcb44aa597b81a2bde967d4c37b60dce2", "b0f6afcdacc53fd91b37ad0c5d70544f7e37ebde", "b7bb797e5c7bdc93f2ffca39baf6e112e6dddbe2", "b7d396b8b6e8f27a1f4556d58fc903321e8d532a", "bf5c2eef34a314d5d2298fdcb6deaa7282cc563f", "cdaa6a951b8fce67673bec224a7ad1f24f0a05bf", "de74034ddf6f92505ccdb61be413a6df2c723f87", "e3ece2fad9e1e6d47b5f06815ff186d7e6f44948", "fd4f0111188b62773b99ab6eab38b4d2b5d8d727", "ff0e61166d3dca59351e3913e0360c24d1bce99c",
                // Recalls
                "097947fd3bd280fcf621e36154a9d4f82896ff01", "2636105c5e363f14cda890f19ac9c3bc57556dd2", "4adc9a9117e7f7501d94c22c8cbbfeb77ad5d596", "560951e9881b5f772262804384b4da9f673b925e", "af0ad6561c0d75c4f155b07e9cfc36a983af55bd", "c39936ee26f00d8256c79df07096f03196811df5", "d9ca2459172a3ad62f0a19b8b1306d739c4b75b7", "e9bf93d510a6a0c105d8f5d036ec35c7ce08a588"
        };
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }


    private static void atom() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\atom";
        String[] commitIds = new String[]{"062dfdea80861f9c918c99a3f86895f908635862", "113453a219e692fe1e5f0eb3c1c09703daaa0a89", "1cdb804824546e8572912df8cf51c9d5072c33ba", "48d36776648149c4170fed630207ef9ec1cb962a", "4dbfadd0d5a603fec292519c9115c857e26201eb", "72c30d7e78f94e432874b54e2c9d71857a06211a", "7ce5b000e448552bb4ba9556c8f38ccfef127162", "adbd5400295494173e59285bbcb6ac967ed3ebfb", "bf6a4e3db4b28bfe068feb72199746dad98d6a83", "cb783fd15cea1d06b254799e7af8ff71d7d7ed33", "e2c480847c81f6f97cc184cb5353080ffbcd590d", "f6d2d5729944abbff8a2db1ab2a35d223e998b8c",
        // Recalls
                "50088b16c9a4e6fda78ea98430bb0705229883e8", "dec52a7384d9f241cae0cd2d0a51f440bafa7677"};
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }

    private static void axios() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\axios";
        String[] commitIds = new String[]{"0e2f4f14171ccd161e85478c7b1aba61c3b431a2", "1629a026da17a1e1d8999a02f3fe6b6b60aaac9c", "1e2cb9bdca0e19298aa7c242a7db4b45427967ea", "203cbc2da0ef0f68d517ba2a6176df1d48efc2fb", "2797f10ea5d2cd963a8e5c80da319848bad9f499", "3d65057d7111d7873cf4fad40805cd9ebe327574", "46e275c407f81c44dd9aad419b6e861d8a936580", "4ac0fbd1c1dc0c5f9ac290b635d6eee3783e355b", "4d1269cb4a9773db128f459046b6c4c2a0926859", "61617543268b024e42996e20ff0435b94a893c5d", "72c66dfdecbcc165c52fcd19d1f8444ad8c011c6", "7ec97dd26b3af7bb0995eef178c4edd8989c3152", "986647be59259125750fd44dbb429658d67dc156", "a59bc8d2ae218484cdcb1d7e6a295a4ff93225a1", "b10874fa67ed77dd6ecbe1fd1f6eb2045ce9854f", "bce07e53aa681367082ce9f683de366918647cd6", "ca0617061e77f8fca502cacd0cced92242b3712e", "cd0cd1805434dea0d250d195a466a1236b98e502", "d23f9d5d4782e5849362895f8b648ed587999706", "dcbb35226245483930a1f2969927a3b348bb2558", "df6d3ce6cf10432b7920d8c3ac0efb7254989bc4", "e8c5c49ea2f2cf4fd45eaf81270a6d23546e2c93", "fc12b933f729f236b2149872c42961a8626dafb7", "ff919487e13430098d3da37a37cc04c3f24b59c4"};
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }

    private static void chartJsExample() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\chart-js";
        String[] commitIds = new String[]{

                // All
                "03735563f44e1ae108f83b9c7ee946f8818c92c3", "16bcd6adc579cb3deae16ea915680bc219924cdc", "182270ef9b1bc9fab1cefc89ce1e94a41f4d754f", "1833614e1d8a82c6c111ce8d1d31eba6df22e7ee", "1cbba830fdef5ac25f997deb5ae0683db9321a79", "1eeffa3d584cdd8d1f54b7455487ae9b13478ebe", "2409908027a3762f142a04e7583bd2e88681a867", "26c1936dee7dcc2f00b63af9bb04e9af1703e461", "2922dc96cfd3c3448538c19c02b349fe939f16eb", "2e5df0ff42c9ec542bd7c88de8f88f50a8ad5864", "312773ba7b7e2acf6cd1273c92017d3c197a992b", "3187a788e17f79ad69da509748aeb64cdbac48f0", "326991ce50c36fd714aefc5bafd4cbe53806051f", "333f2eba99e29ae643671edbef08a245cae9743c", "35dcfe00b1ae7199f8ed6c3748a72f4700c9876d", "447ca40a7f8baa2ec55f3388e02cabcfe8cbfadc", "4741c6f09da431e75fa860b4e08417a4d961fadf", "4a5b5a0e7eba85ca44f658375cb0c78e6af93e5c", "4b421a50bfa17f73ac7aa8db7d077e674dbc148d", "5196e0506257c3242483c511bb558f3f96023f51", "548edc65ea96ac51ab17bde2942fd6f319f63e8c", "56050dc9b7a17130efdbdcd3b8de46b728f25b84", "586b8c12fc546aabcac341b3a949b3891019bd36", "5ad1b4ade7d78eced9c98c4beb678861a41039b7", "5d95280d7b9c910f944ed7e219193a904bb4425b", "6269e2c437a8beccc2516a1635491ab60371950f", "65a06e473560a4d923ed52d4437c3666662d44a6", "6bea15e7cf89003e3a5945a20cf1d2cc5096728e", "6e57405a0a7885ec5099e8b8bd7b26822effa628", "6ec6a929f06c01c053d75378030724749f50bcc9", "717e8d950ac5e0473bc11c1890cde3b53651a8cb", "7205ff5e2aa4515bae0c62bb9d8355745837270e", "73b8ceeb3548bbe8863486a8e254b52a95ac8066", "7756bedec6937eb7a65b7bc530dd2010d0901efe", "7e5e29e3ee15100de599124b4951d50f3aad5f57", "80bd08bef9f3447738c8fde4c0d0b436312a2cb9", "889ecd560bba46a81a29ca29d02f0691aaadc8d2", "8ac0257f8da13afffc3e5d32c9b6debea8f6f6a0", "9015e72ae1ddebe38ba90a538ee2556a46ee3daa", "979341ecb094d9c6a95de8a47e7836f01587e7d2", "9f3b51a80ce96578718267711e8b65c1ec8c25c1", "a0077d41178e576aa473490b13adf2c695bf2faa", "a0a195f353467a71cbf73aef086249902c53c5a4", "a86c47cf480e8d86ea03a121e9b6552a17aae41d", "b39c0e1f93518f2dcb1d1cc49ff04cff36d34a46", "b64cab004669a010025e4641eb7f359c4035f6b9", "b92b256872e9d01ef56c9b8d4440cda784662fe0", "bee8e3cd9bbe943c531a24c808c36870da51a75f", "c216c0af769c9b12bad39b5b889867d7294dee07", "ce27fe5ea6040523a61cef198ccc1e866d07ad82", "d09a17a2b163df7280491ad6c5999d81e892f537", "d21a853f30a87a4cb0fe6c6f2bb39320c0404c19", "d2846864527898225bb8061cf24793101ef98dbf", "d81afc8b5a0f4f27d9d81f1dd748a7a01e7365c0", "ecc35c527b8d91c2bdbc2cdc97ef667adfe7e3a3", "f0bf3954fe363eda86a3db170063aba34f1be08e", "f3816b560c0cec6736b47c456c473e46bdc56498", "f97cab12b1c4db4d5782d92f95e362093ad31860", "fb3ea03440769a267880ba8721d14a3939792718",
                // Recalls
                "b64cab004669a010025e4641eb7f359c4035f6b9", "ec7b87d69c6168d25166784796f8026b2cb5715e",
        };
        new JSRefactoringMiner().detectAtCommits(repoPath, Arrays.asList(commitIds));
    }

    private static void createReactApp() throws Exception {
        String repoPath = "E:\\PROJECTS_REPO\\create-react-app";
        String[] commitIds = new String[]{
                // All
                "00ed100b26adc519fd90e09ebffd83c8d7dc4343", "0e51eef6d7694fbfc4a8fe952bd73b65af2220dd", "2c34d5b66eab7d1c96e573bc48b8e82b6d8e82b0", "3a0b836be376575b5227a0237e8b2334a9f9ab24", "51d0df490295b70808b61d780a54ebcf296a8db2", "72b6eb8c3c65e6ed0f2413708069287311a386c2", "78dbf7bf2b50a35c426e5dbbfdf20ef1afcd8789", "9559ba486e085b6956580ebbc6dad1d7ef4c26e2", "9c167a42490d134832e6e1b721292ea99eaf8ca5", "b3527d7783b290d37275925a1d6a7ff9e40e7a86", "cd3d04b71e91f533bdbdc3856775e1da81d445cf", "d49ffde4e627eea1f935ebc4f8fb2ea61010577d", "d72678fb0c02145d24b7684802a3f5cfc94bd746", "ecd1f0544b6f8a05a4061712932cd0055d6e34c9", "eed708a822d6cf3ff17b665b093a2c23a50f6b15",
                // Recalls
                "fbdff9d722d6ce669a090138022c4d3536ae95bb",
        };
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
