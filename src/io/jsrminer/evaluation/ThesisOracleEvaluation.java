package io.jsrminer.evaluation;

import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.util.RefactoringDisplayFormatter;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class ThesisOracleEvaluation {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    StringBuilder builder = new StringBuilder();
    StopWatch watch = new StopWatch();

    public Map<String, String[]> projectRandomCommmitMap = new LinkedHashMap<>();
    public Map<String, Long> commitRunTime = new LinkedHashMap<>();
    public Map<String, String> projectRepoPathMap = new LinkedHashMap<>();

    private static  final  String REPOS_PATH =  "D:\\PROJECTS_REPO";
    private void createProjectRepoMap() {
        projectRepoPathMap.put("angular.js", REPOS_PATH + "\\angular.js");
        projectRepoPathMap.put("atom", REPOS_PATH + "\\atom");
        projectRepoPathMap.put("axios", REPOS_PATH + "\\axios");
        projectRepoPathMap.put("Chart.js", REPOS_PATH + "\\chart-js");
        projectRepoPathMap.put("create-react-app", REPOS_PATH + "\\create-react-app");
        projectRepoPathMap.put("d3", REPOS_PATH + "\\d3");
        projectRepoPathMap.put("express", REPOS_PATH + "\\expressjs");
        projectRepoPathMap.put("jquery", REPOS_PATH + "\\jquery");
        projectRepoPathMap.put("material-ui", REPOS_PATH + "\\material-ui"); //exp
        projectRepoPathMap.put("meteor", REPOS_PATH + "\\meteor");
        projectRepoPathMap.put("react", REPOS_PATH + "\\react"); //exp
        projectRepoPathMap.put("react-native", REPOS_PATH + "\\react-native");
        projectRepoPathMap.put("redux", REPOS_PATH + "\\redux");
        projectRepoPathMap.put("reveal.js", REPOS_PATH + "\\reveal.js");
        projectRepoPathMap.put("Semantic-UI", REPOS_PATH + "\\Semantic-UI");
        projectRepoPathMap.put("socket.io", REPOS_PATH + "\\socket.io");
        projectRepoPathMap.put("three.js", REPOS_PATH + "\\three.js");
        projectRepoPathMap.put("vue", REPOS_PATH + "\\vue");
        projectRepoPathMap.put("webpack", REPOS_PATH + "\\webpack");
    }

    public static void main(String[] args) {
        var evaluator = new ThesisOracleEvaluation();
        evaluator.createMap();
        evaluator.createProjectRepoMap();
        //evaluator.runAll();
        evaluator.run("angular.js", allProjectCommits);
       //evaluator.run("angular.js", "03043839d5a540b02208001fe12e812dfde00a8e");
       //evaluator.run("Semantic-UI", "7e37d4a098a51c0a888ca273b07d1423e21eef7c");
    }

    public void runRandomized() {
        builder.setLength(0);
        for (var project : projectRepoPathMap.keySet()) {
            run(project, projectRandomCommmitMap);
        }
        log.info("\n" + RefactoringDisplayFormatter.getHeader() + "\n" + builder.toString());
        printCommitRuntime("random");
    }

    public void runAll() {
        builder= new StringBuilder();
        StringBuilder allBuilder = new StringBuilder();
        String projectRefs = null;
        allBuilder.append(RefactoringDisplayFormatter.getHeader() + "\n");
        log.info("\n" + RefactoringDisplayFormatter.getHeader() + "\n");
        for (var project : projectRepoPathMap.keySet()) {
            run(project, allProjectCommits);
            projectRefs = builder.toString();
            log.info(projectRefs);
            allBuilder.append(projectRefs);
            builder = new StringBuilder();
        }
        //log.info("\n" + RefactoringDisplayFormatter.getHeader() + "\n" + builder.toString());
        try {
            Files.writeString(Path.of("resources\\evaluation\\projects_run_log\\" + "all" + ".txt"), allBuilder.toString(), StandardOpenOption.CREATE);
        } catch (Exception ex) {

        }
        printCommitRuntime("all");
    }

    public void run(String project, Map<String, String[]> projectCommmitMap) {
        var repoPath = projectRepoPathMap.get(project);
        var commitIds = projectCommmitMap.get(project);
        var jsrminer = new JSRefactoringMiner();
        for (int i = 0; i < commitIds.length; i++) {
            var commitId = commitIds[i];
            watch.start();
            var refactorings = jsrminer.detectAtCommit(repoPath, commitId);
            watch.stop();
            commitRunTime.put(commitId, watch.getTime());
            watch.reset();
            String commitStr = RefactoringDisplayFormatter.generateDisplayStringForRefactorings(project, commitId, refactorings, false);
            builder.append(commitStr);
        }

        log.info("\n" + RefactoringDisplayFormatter.getHeader() + "\n");
        var result = builder.toString();
        log.info(result + "\n");
        printCommitRuntime(project);

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(RefactoringDisplayFormatter.getHeader() + "\n");
            sb.append(result);
            var path = Path.of("resources\\evaluation\\projects_run_log\\" + project + ".txt");
            Files.deleteIfExists(path);
            Files.writeString(path, sb.toString(), StandardOpenOption.CREATE);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void run(String project, String commitId) {
        var repoPath = projectRepoPathMap.get(project);
        watch.start();
        var refactorings = new JSRefactoringMiner().detectAtCommit(repoPath, commitId);
        watch.stop();
        commitRunTime.put(commitId, watch.getTime());
        watch.reset();
        String commitStr = RefactoringDisplayFormatter.generateDisplayStringForRefactorings(project, commitId, refactorings, false);
        builder.append(commitStr);
        log.info("\n" + RefactoringDisplayFormatter.getHeader() + "\n" + builder.toString());
        printCommitRuntime(project);
    }

    public void createMap() {
        projectRandomCommmitMap.put("Chart.js", new String[]{
                "1833614e1d8a82c6c111ce8d1d31eba6df22e7ee", "56050dc9b7a17130efdbdcd3b8de46b728f25b84", "5d95280d7b9c910f944ed7e219193a904bb4425b", "7756bedec6937eb7a65b7bc530dd2010d0901efe", "889ecd560bba46a81a29ca29d02f0691aaadc8d2", "979341ecb094d9c6a95de8a47e7836f01587e7d2", "a0077d41178e576aa473490b13adf2c695bf2faa", "a86c47cf480e8d86ea03a121e9b6552a17aae41d", "b39c0e1f93518f2dcb1d1cc49ff04cff36d34a46", "ce27fe5ea6040523a61cef198ccc1e866d07ad82"
        });
        projectRandomCommmitMap.put("create-react-app", new String[]{
                "0e51eef6d7694fbfc4a8fe952bd73b65af2220dd", "2c34d5b66eab7d1c96e573bc48b8e82b6d8e82b0", "72b6eb8c3c65e6ed0f2413708069287311a386c2", "9559ba486e085b6956580ebbc6dad1d7ef4c26e2", "9c167a42490d134832e6e1b721292ea99eaf8ca5", "cd3d04b71e91f533bdbdc3856775e1da81d445cf", "d49ffde4e627eea1f935ebc4f8fb2ea61010577d", "d72678fb0c02145d24b7684802a3f5cfc94bd746", "ecd1f0544b6f8a05a4061712932cd0055d6e34c9", "eed708a822d6cf3ff17b665b093a2c23a50f6b15"
        });

        projectRandomCommmitMap.put("d3", new String[]{
                "0a39765f3cbcb940e6fb354cec010a7ffeba9289", "2eba0320407442e85be97b2cd84398fb6c626fe3", "959da21882ad7ea5f35f851adb629ae7a29d5a38"
        });
        projectRandomCommmitMap.put("express", new String[]{
                "12bc16e72fe34ce7e3658679b694e47dce56ae7a", "4012846d2534addd8de0b0c237cd63ada104109a", "5312a990b920d5ce1313fafaebcac2d440146c16", "d9d09b8b9041504b645f3173ca70ef173c7e1563", "dab9222942cb9f7c3ff17abad4e0f6137c9e2cfc", "f5a240636d71781c7875191d2048279efce5ad56", "f90f9dde3fe02cd9f4b081c16441415817b297b0"
        });
        projectRandomCommmitMap.put("jquery", new String[]{
                "022b69a44e42684bdd0029dd456bedb3b495cc24", "1f4375a34227f42570d2b72e190e51bcfb1e8597", "3a8e44745c014871bc56e94d91e57c45ae4be208", "3d732cca6b5076a9d13eee98e2b075b37384cd91", "67c96a59f5af9e8404b3f904028e1c730d647498", "6d43dc42337089f5fb52b715981c12993f490920", "80f57f8a13debaab87b99f73631669699da3e1a5", "bf591fb597a056bf2fc9bc474010374695b18d1a", "e4fd41f8fa4190fbbb6cb98cf7ace64f6e00685d", "ecd8ddea33dc40ae2a57e4340be03faf2ba2f99b"
        });
        projectRandomCommmitMap.put("material-ui", new String[]{
                "1b66305162e232e8f7ce7a91f31274647a1dd4bb", "2869966d8239365451a92cb044e435c671211786", "397540b2acb289a55e4557c8852935eead88b995", "466c01fc7e7bc76adf5ad34da125daff43a1b206", "70c0ed39d9fc72ee74492d9e5903aa94319604c7", "7107751c8e1407aa098882047cdd36e7372c5843", "7796fb2677b547bec838eeaf4c219ffb00825c49", "a423d45bbe36995dd3f372d07b0af0fc8a6f5ba3", "ad4df1c16a9070582250db941dd97854215bc31f", "f4be182606fe060b10ca0107055a6704d6c208e2"
        });
        projectRandomCommmitMap.put("meteor", new String[]{
                "39d8aef3d96c3f744e926f87ee05d67a8e2f58d5", "6f93bd1def8167af18ce84daa06c2547822220b2", "8a8db83d298a932ed72157ee0162792d9f592cd9", "91a4a46ea1d687de1f929e3b9f0bae9c2db0c83d", "970d58409fc2a7b7a78bbc7098aef24c38f1f249", "9d65f9269982e066de112ccf7e22dfeb2528ba58", "a212d9f5158e9f9b8022eb88afc1aa77119efda2", "a9af151df923737f044d98a14ac47f249a39ba79", "b50a52ecf525ab861be648562c952a2f45b6a1e0", "d4d3df14285e559c92d5294b04be97ebb26517fd"
        });
        projectRandomCommmitMap.put("react", new String[]{
                "036ae3c6e2f056adffc31dfb78d1b6f0c63272f0", "0deea326674077598e351803d7a204a1c744a578", "2f7bca0eb2487955e71a45e288e5847b5af522a5", "36c29393720157a3966ce1d50449a33a35bdf14c", "3766a014ae7752be70ca12b287637d8960dee6c3", "3d8f465d99ece19238ccb561cdb157d2d676dda4", "45b90d48668dae48aad611819b891d029aa2fb27", "b4b21486aa043b3c6260665930e7e638c908a5ec", "cda9fb04999bb0cc2dd148449748556464f3bf74", "f35d989bea2ee38c164c0cbb23cd300552e998f9"
        });
        projectRandomCommmitMap.put("react-native", new String[]{
                "06c05e744d8af9582bde348210f254d76dae48b9", "4435f087713e1d0ac3639e3b3196d71c6402898e", "488a4c7e1c86ac5900ff9194106511fbf5a8e3cb", "5259450c143f71c65e157d6b7d3f0e1655eb7aa1", "7014a30baae0b01378740113d15e4a7d721d916a", "95e8592dcb9b50772336d2801f46305fdc95250d", "9fbff62b5ff9b3effb18f135e1cd9bd34a83c985", "be32cbef007dada23bf830b3b12df8beefbbdac6", "f80000b9e72596392567a3deda59441e43f6327d", "fbd1beaf666be9c09a380784f8c0cd34ba083a6b"
        });
        projectRandomCommmitMap.put("redux", new String[]{
                "27d9a24d1020685bc723f91ef60d0907df58ec23", "89b0c254ad806c9707fe8f2d3cd78b191b30d2c6", "acc10fac4bd381cbf143f9488ac886ed1475b19a", "b1c6569407f77d8cb7f27b8548eefee53a5b9634", "c4e6c3228e9cb8bd37d12756ec9049acc65007e1", "c9ad6a40fe501919d359b910ea7c7f34532e11d9", "e238a19767c750a49b5f94f8439c25ac04511c80", "e2e9648b264224af68add35431898dafe26b0a09", "e5e608eb87f84d4c6ec22b3b4e59338d234904d5", "fa3b2e4b56aa522de99d8848a0c13ea6fa860541"
        });
        projectRandomCommmitMap.put("reveal.js", new String[]{
                "0338f280d3942094782da57ea3b5b72bdb833e9d", "0563835fc00ce2c255cad0e4a089f559cdd48964", "0d37757f3f34dff5840eff6bd5ac6735938234a6", "5f90a449cf1fa6edc93aaa4e69e6152c6096cd2e", "a03e8036289a49f0b4e0b43b1d8547f9ee439d42", "af270a909cdbf0de6edd25dfac4aeae0a58e8ab7", "e29c706533c227682cfde1ac0b187e90738b9bbc", "e3a3d3aa0aa1806e20bffb0ff4c307ec6cc89964", "ef9cbbbbb92560f1ffd41dce23a584474922fe16", "fab28362ce2f46929c41cc1252281a7757b4695f"
        });
        projectRandomCommmitMap.put("Semantic-UI", new String[]{
                "1b48f527eb73d6bc4b1af2e94d52f51c32cec3c3", "2c26746dcedcc1610ab2c4ce0b64c218ab324547", "3b802c33c1aeb7473637e0362f26670c53158d3f", "6bc1c5ec4bdaf349e8ac5877f9fee0139a138d9d", "775df0e06179e5330a684379eb8b604718e41ed5", "7e37d4a098a51c0a888ca273b07d1423e21eef7c", "8b38f91c51ab112897b2e4e63e5acd6f85cbdd8f", "9f1f6ab4cda3742e472a809dae71a2555a2ce354", "aa0c9ce453519bd339ea9adfd61f8763a0336c82", "bd6e37c5d0b1384e60ba552c9e517a1e938d26b6"
        });
        projectRandomCommmitMap.put("socket.io", new String[]{
                "3aa3213b13e914a668a76bc5eab1cef80708bf01", "3b5f4339a7b73c449684707f021c3f4aa2e01b13", "44a79f9cee892f607d038a9949243f91a2aa31e6", "463d7a16a139981f1eead005b388f720be0989bd", "5a123beea597c9fda7b722f18343fdc2c2755e79", "852a9d34dfae0fa9344805105511cb18d1e375a3", "8a3781499d3c88cdcafb90fbcd762e04da5552f0", "94df7bcdfd5696c63aff496d850ff0305b019e67", "c0c79f019e7138194e438339f8192705957c8ec3", "fc34c7d7a489a37a83de2e2af6ffcc41351110fa"
        });
        projectRandomCommmitMap.put("three.js", new String[]{
                "06903c44a2d9c8cb0a57373999e0596aa51d5627", "27a5b9a172a532c54e9ad75bdaa7434652409198", "35ae830a7c4544582ed2759e5b18c5d6ef37c6d9", "7f0a1234c2d6726f625740595455ff5274f7d5f6", "99d5b58a771fc09377d7393e913fc641f5d9aece", "a4b52f8cb5569c69d3801e5b4ba236cd75fdfde9", "b55374012d1842c3c536b757adc7ad9ff5caa68d", "cc3d60f8d860326264ca7416b25ffca5386a9a27", "f0e7bdc1de54a1b896089d819872111a86aa4185", "f3f33b5c5a661c04062cd0b8bf98f74d85e2abb7"
        });
        projectRandomCommmitMap.put("vue", new String[]{
                "50b711af43708426e63b4ea529436b49fafc3f2e", "5db86b4e94857fdde3ae6b71e24da637bc116baa", "653aac2c57d15f0e93a2c1cc7e6fad156658df19", "7ad368ebb6987bd4044f9df184d73ce14ca680f2", "8335217cb4bd13fb07e08a76c07df0fceed6c197", "b5cc8b9e935897184ac43119bf5fbeca606f4b6a", "bc2918f0e596d0e133a25606cbb66075402ce6c3", "bc719405c084afb5dd4168413d880a4b801e234f", "c104cc582d647f5e10b90563cb80907b9e30ec12", "df82aeb0bf7454ac99d403000a1ac993e8d8d4de"
        });
        projectRandomCommmitMap.put("webpack", new String[]{
                "35130585ae6a7a346234c27ba379a4280006452c", "8636670169131f0617713eb012c41f2c04413430", "86aa18d6990f4102704854ad5dc76b1cc45323f0", "86c00207bdc9cb1ef60441d1ec836624a162c9ab", "9156be961d890b9877ddef3a70964c9665662abb", "9c7100ba6023347cd2ded17ec16900753b01c18d", "9cb1a663173f5fcbda83b2916e0f1679c1dc642e", "da6f869c1ef39abd0f1e512b07f41f6bbecd4b88", "e4836826373151c8d14db011a1c3f2763aeb727a", "e6562319fecf9ce5230876c504bfa495ce3bdabd"
        });
    }

    void printCommitRuntime(String project) {
        StringBuilder builder = new StringBuilder();
        builder.append("commit_id\tms\n");
        for (var entry : commitRunTime.entrySet()) {
            builder.append(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        var str = builder.toString();
        try {

            System.out.println(str);
            var path = Path.of("resources\\evaluation\\performance\\" + project + ".txt");
            Files.deleteIfExists(path);
            Files.writeString(path, str, StandardOpenOption.CREATE);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final Map<String, String[]> allProjectCommits = Map.ofEntries(
            entry("angular.js", new String[]{"03043839d5a540b02208001fe12e812dfde00a8e", "05fdf918d97a379634c8b0e15da8615edfc6fb8d", "097947fd3bd280fcf621e36154a9d4f82896ff01", "0cbc50512126fa22546dbe9b79a14939d9dc4459", "0ce9348bc01811a88e0e19f85e5bf74536ad4dd4", "189461f9bf6fda18ddbd16c42f2e959cf939c3da", "1e90d030771b68d55dffd56898a380f903712b66", "1e9eadcd72dbbd5c67dae8328a63e535cfa91ff9", "223de59e988dc0cc8b4ec3a045b7c0735eba1c77", "2636105c5e363f14cda890f19ac9c3bc57556dd2", "2ee5033967d5f87a516bad137686b0592e25d26b", "38f8c97af74649ce224b6dd45f433cc665acfbfb", "42e622b751d1ad1520b824473f9ad31e7efb75b3", "4a12ae7b3617e7c5389cf67ce3e3c7b309fcd9b9", "4adc9a9117e7f7501d94c22c8cbbfeb77ad5d596", "538f4606ff32e776b093243fed4af3460df74f2a", "560951e9881b5f772262804384b4da9f673b925e", "5878f07474755cb3df1e727cef4e7e4716f44783", "60069e67aeb0352a7849ebfef15a6183bc3b7235", "641e13acc180000a8b1293a1bf9dfef29673e2ea", "667d4734fcf7a58a58d29bd87fac32a7831df184", "7e2e235cf25389a59ea957631ff07c6cc8a3ad32", "817ac56719505680ac4c9997972e8f39eb40a6d0", "837acd14e005c79d8e79f59a0075075f125da50c", "840b5f0a7634efdf4d6ed81a52e66fc0e0620127", "8d6ac5f3178cb6ead6b3b7526c50cd1c07112097", "8e104ee508418bc2ebb65e5b4ac73d22285cc224", "9d74f0fdcb44aa597b81a2bde967d4c37b60dce2", "af0ad6561c0d75c4f155b07e9cfc36a983af55bd", "b0f6afcdacc53fd91b37ad0c5d70544f7e37ebde", "b7bb797e5c7bdc93f2ffca39baf6e112e6dddbe2", "b7d396b8b6e8f27a1f4556d58fc903321e8d532a", "bf5c2eef34a314d5d2298fdcb6deaa7282cc563f", "c39936ee26f00d8256c79df07096f03196811df5", "cdaa6a951b8fce67673bec224a7ad1f24f0a05bf", "d9ca2459172a3ad62f0a19b8b1306d739c4b75b7", "de74034ddf6f92505ccdb61be413a6df2c723f87", "e3ece2fad9e1e6d47b5f06815ff186d7e6f44948", "e5fb92978f310d690e394bac2c09beeae4a56e03", "e9bf93d510a6a0c105d8f5d036ec35c7ce08a588", "fd4f0111188b62773b99ab6eab38b4d2b5d8d727", "ff0e61166d3dca59351e3913e0360c24d1bce99c"}),
            entry("atom", new String[]{"0148ee7d9bc731e98365fbaec53e4b8a5fbfa5c9", "062dfdea80861f9c918c99a3f86895f908635862", "113453a219e692fe1e5f0eb3c1c09703daaa0a89", "1cdb804824546e8572912df8cf51c9d5072c33ba", "48d36776648149c4170fed630207ef9ec1cb962a", "4dbfadd0d5a603fec292519c9115c857e26201eb", "50088b16c9a4e6fda78ea98430bb0705229883e8", "547b067f3e03f7d3e7eb162af8f257cfe6f05154", "72c30d7e78f94e432874b54e2c9d71857a06211a", "7c9463aceae23a9bd7b88b141994d87fd4c08779", "7ce5b000e448552bb4ba9556c8f38ccfef127162", "adbd5400295494173e59285bbcb6ac967ed3ebfb", "bf6a4e3db4b28bfe068feb72199746dad98d6a83", "cb783fd15cea1d06b254799e7af8ff71d7d7ed33", "dec52a7384d9f241cae0cd2d0a51f440bafa7677", "e2c480847c81f6f97cc184cb5353080ffbcd590d", "f6d2d5729944abbff8a2db1ab2a35d223e998b8c"}),
            entry("axios", new String[]{"0e2f4f14171ccd161e85478c7b1aba61c3b431a2", "1629a026da17a1e1d8999a02f3fe6b6b60aaac9c", "1e2cb9bdca0e19298aa7c242a7db4b45427967ea", "203cbc2da0ef0f68d517ba2a6176df1d48efc2fb", "2797f10ea5d2cd963a8e5c80da319848bad9f499", "3d65057d7111d7873cf4fad40805cd9ebe327574", "46e275c407f81c44dd9aad419b6e861d8a936580", "4ac0fbd1c1dc0c5f9ac290b635d6eee3783e355b", "4d1269cb4a9773db128f459046b6c4c2a0926859", "61617543268b024e42996e20ff0435b94a893c5d", "72c66dfdecbcc165c52fcd19d1f8444ad8c011c6", "7ec97dd26b3af7bb0995eef178c4edd8989c3152", "986647be59259125750fd44dbb429658d67dc156", "a59bc8d2ae218484cdcb1d7e6a295a4ff93225a1", "b10874fa67ed77dd6ecbe1fd1f6eb2045ce9854f", "bce07e53aa681367082ce9f683de366918647cd6", "ca0617061e77f8fca502cacd0cced92242b3712e", "cd0cd1805434dea0d250d195a466a1236b98e502", "d23f9d5d4782e5849362895f8b648ed587999706", "dcbb35226245483930a1f2969927a3b348bb2558", "df6d3ce6cf10432b7920d8c3ac0efb7254989bc4", "e8c5c49ea2f2cf4fd45eaf81270a6d23546e2c93", "fc12b933f729f236b2149872c42961a8626dafb7", "ff919487e13430098d3da37a37cc04c3f24b59c4"}),
            entry("Chart.js", new String[]{"03735563f44e1ae108f83b9c7ee946f8818c92c3", "16bcd6adc579cb3deae16ea915680bc219924cdc", "182270ef9b1bc9fab1cefc89ce1e94a41f4d754f", "1833614e1d8a82c6c111ce8d1d31eba6df22e7ee", "1cbba830fdef5ac25f997deb5ae0683db9321a79", "1eeffa3d584cdd8d1f54b7455487ae9b13478ebe", "2409908027a3762f142a04e7583bd2e88681a867", "26c1936dee7dcc2f00b63af9bb04e9af1703e461", "2922dc96cfd3c3448538c19c02b349fe939f16eb", "2e5df0ff42c9ec542bd7c88de8f88f50a8ad5864", "312773ba7b7e2acf6cd1273c92017d3c197a992b", "3187a788e17f79ad69da509748aeb64cdbac48f0", "326991ce50c36fd714aefc5bafd4cbe53806051f", "333f2eba99e29ae643671edbef08a245cae9743c", "35dcfe00b1ae7199f8ed6c3748a72f4700c9876d", "447ca40a7f8baa2ec55f3388e02cabcfe8cbfadc", "4741c6f09da431e75fa860b4e08417a4d961fadf", "4a5b5a0e7eba85ca44f658375cb0c78e6af93e5c", "4b421a50bfa17f73ac7aa8db7d077e674dbc148d", "5196e0506257c3242483c511bb558f3f96023f51", "548edc65ea96ac51ab17bde2942fd6f319f63e8c", "56050dc9b7a17130efdbdcd3b8de46b728f25b84", "586b8c12fc546aabcac341b3a949b3891019bd36", "5ad1b4ade7d78eced9c98c4beb678861a41039b7", "5d95280d7b9c910f944ed7e219193a904bb4425b", "6269e2c437a8beccc2516a1635491ab60371950f", "65a06e473560a4d923ed52d4437c3666662d44a6", "6bea15e7cf89003e3a5945a20cf1d2cc5096728e", "6e57405a0a7885ec5099e8b8bd7b26822effa628", "6ec6a929f06c01c053d75378030724749f50bcc9", "717e8d950ac5e0473bc11c1890cde3b53651a8cb", "7205ff5e2aa4515bae0c62bb9d8355745837270e", "73b8ceeb3548bbe8863486a8e254b52a95ac8066", "7756bedec6937eb7a65b7bc530dd2010d0901efe", "7e5e29e3ee15100de599124b4951d50f3aad5f57", "80bd08bef9f3447738c8fde4c0d0b436312a2cb9", "889ecd560bba46a81a29ca29d02f0691aaadc8d2", "8ac0257f8da13afffc3e5d32c9b6debea8f6f6a0", "9015e72ae1ddebe38ba90a538ee2556a46ee3daa", "979341ecb094d9c6a95de8a47e7836f01587e7d2", "9f3b51a80ce96578718267711e8b65c1ec8c25c1", "a0077d41178e576aa473490b13adf2c695bf2faa", "a0a195f353467a71cbf73aef086249902c53c5a4", "a86c47cf480e8d86ea03a121e9b6552a17aae41d", "b39c0e1f93518f2dcb1d1cc49ff04cff36d34a46", "b64cab004669a010025e4641eb7f359c4035f6b9", "b92b256872e9d01ef56c9b8d4440cda784662fe0", "bee8e3cd9bbe943c531a24c808c36870da51a75f", "c216c0af769c9b12bad39b5b889867d7294dee07", "ce27fe5ea6040523a61cef198ccc1e866d07ad82", "d09a17a2b163df7280491ad6c5999d81e892f537", "d21a853f30a87a4cb0fe6c6f2bb39320c0404c19", "d2846864527898225bb8061cf24793101ef98dbf", "d81afc8b5a0f4f27d9d81f1dd748a7a01e7365c0", "ec7b87d69c6168d25166784796f8026b2cb5715e", "ecc35c527b8d91c2bdbc2cdc97ef667adfe7e3a3", "f0bf3954fe363eda86a3db170063aba34f1be08e", "f3816b560c0cec6736b47c456c473e46bdc56498", "f97cab12b1c4db4d5782d92f95e362093ad31860", "fb3ea03440769a267880ba8721d14a3939792718"}),
            entry("create-react-app", new String[]{"00ed100b26adc519fd90e09ebffd83c8d7dc4343", "0e51eef6d7694fbfc4a8fe952bd73b65af2220dd", "2c34d5b66eab7d1c96e573bc48b8e82b6d8e82b0", "3a0b836be376575b5227a0237e8b2334a9f9ab24", "51d0df490295b70808b61d780a54ebcf296a8db2", "72b6eb8c3c65e6ed0f2413708069287311a386c2", "78dbf7bf2b50a35c426e5dbbfdf20ef1afcd8789", "9559ba486e085b6956580ebbc6dad1d7ef4c26e2", "9c167a42490d134832e6e1b721292ea99eaf8ca5", "b3527d7783b290d37275925a1d6a7ff9e40e7a86", "cd3d04b71e91f533bdbdc3856775e1da81d445cf", "d1454febd13c8e008e5dec4f13e7d55bf4eb4d18", "d49ffde4e627eea1f935ebc4f8fb2ea61010577d", "d72678fb0c02145d24b7684802a3f5cfc94bd746", "ecd1f0544b6f8a05a4061712932cd0055d6e34c9", "eed708a822d6cf3ff17b665b093a2c23a50f6b15", "fbdff9d722d6ce669a090138022c4d3536ae95bb"}),
            entry("d3", new String[]{"0a39765f3cbcb940e6fb354cec010a7ffeba9289", "2eba0320407442e85be97b2cd84398fb6c626fe3", "959da21882ad7ea5f35f851adb629ae7a29d5a38"}),
            entry("express", new String[]{"12bc16e72fe34ce7e3658679b694e47dce56ae7a", "4012846d2534addd8de0b0c237cd63ada104109a", "5312a990b920d5ce1313fafaebcac2d440146c16", "d9d09b8b9041504b645f3173ca70ef173c7e1563", "dab9222942cb9f7c3ff17abad4e0f6137c9e2cfc", "f5a240636d71781c7875191d2048279efce5ad56", "f90f9dde3fe02cd9f4b081c16441415817b297b0"}),
            entry("jquery", new String[]{"022b69a44e42684bdd0029dd456bedb3b495cc24", "1ea092a54b00aa4d902f4e22ada3854d195d4a18", "1f4375a34227f42570d2b72e190e51bcfb1e8597", "224271982eb9cd351d7db1b38c740b4e927e6f97", "2b5f5d5e90b37f4a735738a6d0b6f22affbea340", "356a3bccb0e7468a2c8ce7d8c9c6cd0c5d436b8b", "39cdb8c9aa0fde68f733553ba050a2ba9d86474c", "3a8e44745c014871bc56e94d91e57c45ae4be208", "3d732cca6b5076a9d13eee98e2b075b37384cd91", "4d6b4536b2e0148d4b228884a0b356e73dd8beec", "59ec78e6020cc963c1f95cb96a28eaaf20e37b3e", "5cbb234dd3273d8e0bbd454fb431ad639c7242c1", "619bf98d5b479f9582dbc40259b666f1c5a83146", "64a289286a743516bce82462200062a647ef3ac0", "67c96a59f5af9e8404b3f904028e1c730d647498", "67d7a2eefee768b59eb3d51cb1fb2c671873e58a", "6d43dc42337089f5fb52b715981c12993f490920", "80f57f8a13debaab87b99f73631669699da3e1a5", "93a8fa6bfc1c8a469e188630b61e736dfb69e128", "9e121482a532d61aa36d7b314ee46dd1ac40f29e", "a4474c9a0025095f82d734a9d7230eace2f08ef8", "a6fc0b16511c70472ebb3dd9469d08566969ac88", "ac9e3016645078e1e42120822cfb2076151c8cbe", "b078a62013782c7424a4a61a240c23c4c0b42614", "b442abacbb8464f0165059e8da734e3143d0721f", "b5f7c9e2d526b17b9962976bb704dce8779d7362", "b930d14ce64937e9478405eee2828d4da091d2cb", "b97c8d30c5aedace75dc17056d429f28e41b20c1", "bd984f0ee2cf40107a669d80d92566b8625b1e6b", "bf3a43eff8682b59cec785be6003753fa4b93706", "bf48c21d225c31f0f9b5441d95f73615ca3dcfdb", "bf591fb597a056bf2fc9bc474010374695b18d1a", "c18d608537d8945de6c5855a9475657177fa74ac", "c4494d4abc84d368d6597889ab45fc07466f8f26", "c9935b6d2db9e1be4bed12f7419e98cdca45763e", "cdaed15c7ea1bbfdde5a5bea691c583ce7961526", "e4fd41f8fa4190fbbb6cb98cf7ace64f6e00685d", "e5ffcb0838c894e26f4ff32dfec162cf624d8d7d", "ecd8ddea33dc40ae2a57e4340be03faf2ba2f99b", "ee0854f85bd686b55757e8854a10480f23c928da"}),
            entry("material-ui", new String[]{"1b66305162e232e8f7ce7a91f31274647a1dd4bb", "1dac1e16bfbcc3913e1fea36de0b1d1e8a2b0478", "2869966d8239365451a92cb044e435c671211786", "2d5cf8c2380a42a2b2aa2e33ea2d6f4c6140ee8c", "397540b2acb289a55e4557c8852935eead88b995", "3e4854b39b17511d9b312071ca93061eeb503f5d", "41b7049bceaa059388b8db4ed0b17c1f6a46aa1f", "466c01fc7e7bc76adf5ad34da125daff43a1b206", "6054e95d4d1b87c97cd7c090371473ced3dc27b1", "6ba2f297382478c5ac931fe50c10fbe7c1724452", "70c0ed39d9fc72ee74492d9e5903aa94319604c7", "7107751c8e1407aa098882047cdd36e7372c5843", "76a594983c2e60b44a02b4a500a8075439a7df56", "7796fb2677b547bec838eeaf4c219ffb00825c49", "8c03090447af8f3d2841afaf4ada6d2195319c44", "90c0ea2cd0415cf71c2310330dd38a36300724fd", "961edfa683355190e34f297855f7f0c0bdf5507a", "96cdbc18b05f1add91c3242be785d0fd2ae7c9ac", "984094fea06cd2aa120856f0b1c36eaa2d9c4842", "a423d45bbe36995dd3f372d07b0af0fc8a6f5ba3", "a537af5fa685f7a6fe4902847a1b12504b749463", "ad4df1c16a9070582250db941dd97854215bc31f", "be064e88ca3d64e950aaa1649cb44f7893478158", "be27408b7f6a940c1804af305eb4f161eca05b5f", "bfe642ef96437edcfa03bb3bfb3ee34f11247556", "c451323da71fa0ec5f7505d14f50d92f0c91f824", "dedcf74c2fb61a9ff7f1a1529f32dbca999cb678", "e199c9fb70622b8a2b4bf42ff6af22029ada2ea6", "e1c0cc77c25cb736963c73bb872131d043c00b7d", "ea0840dccadb92a9fce7bebfe47bbfaf0abd276c", "f4be182606fe060b10ca0107055a6704d6c208e2", "f7ecce35edbdf78972a695209adc0f3b90efebbf", "fb840ff0a91144c2a16e6d48f1f0b17b0a2e8157"}),
            entry("meteor", new String[]{"05ef0a6602b686db7716c0e805672eff1b15a28b", "06863d0203315640e70982e305c632835f8f06f5", "06f9f78277f8cf0ebe217eccd5e54e163fbc8fdf", "14f6f9328bf358b3d953968d29246573d2d05366", "17eaa5bd6f9f4837c6c22001c30bfd65e5d28496", "20e89b9009806ca9cec0e464be1937eecf64a46d", "22e3f995652c8c81e3cc25965d1882834d3ad9dc", "25bddd9a23dda6b309804e052232b09637250f42", "39d8aef3d96c3f744e926f87ee05d67a8e2f58d5", "3a17a032c74ad252cef75cddd1155ec8e84a40af", "48947a46bf7204367dcd3294c93d55e2f36e01eb", "4ad860ca47071c167281888e984f15ba987355bd", "4e074be6a877abbab70d92a0e0920a0118a00212", "5241d67deddc9bd101666ec7c4767c02718d26a8", "552764635e599d47e06f457ebed6aeca1da9cda5", "56ac7657799f6cf49a3743a779984aedc20cc059", "5aab6d2a129a8214ff2d6c22ee7b0b848482a222", "60731a88bcffa5409dbc2377ab5f33b83d6f713c", "640011a0bdb4e374c47de9a8431458d4c5e79601", "643a9f12da263044829f90ea06cbb1d6c1afe91e", "6b1bb038d820cbb4a6a066990da12e4c70d6e68d", "6f93bd1def8167af18ce84daa06c2547822220b2", "72edda6b37cca8e6f112f7d6b06d1ce320c9eb91", "79cb14afbed0bedcdb1dec278e15a278d7d3d2af", "7cad0f5fe1e1bee0d0e2368c6c86923e6ee1dcbc", "7f2a683e2ca14c3998e8e22c58110aed9b30fdc1", "8a8db83d298a932ed72157ee0162792d9f592cd9", "8dd2402f76e519ccadbd99eef39aef6ca6444966", "91a4a46ea1d687de1f929e3b9f0bae9c2db0c83d", "9401f0d572648ad091cfd3f8ac2048db929b549e", "969e1b37f0f22097bc984709d6019bba0c310101", "970d58409fc2a7b7a78bbc7098aef24c38f1f249", "9d65f9269982e066de112ccf7e22dfeb2528ba58", "a212d9f5158e9f9b8022eb88afc1aa77119efda2", "a4ffe2c627688f7043891d20db98c4287b6c0817", "a52b462f557f07d79583792211c289c1c34344ce", "a9af151df923737f044d98a14ac47f249a39ba79", "adc5e40fb74ad2f1b4bea172a1214d46d2e6686c", "afb7d23d045602cd7f9d4b7ae61081c4bcc3b12f", "b50a52ecf525ab861be648562c952a2f45b6a1e0", "b5286b941a77a1bdd57abb1ea01385eeaa62f7ea", "baa6108f0c4b4b4a7f8bbdd1d4a59c0a5a0a50a2", "c9777e3fc4b82e08b8af19c32c3ab013076aea48", "cb77875f3849c6ecb753169d7ecd5193f230e8a4", "ccc6294018f376faa7a57259f21da93a63e3f3da", "ce34a60387db1b783915fdeb298c0b9fdf18bce1", "cf33eeedbc8a3bcda4115f51032d3f6713336101", "d2db339087d4a2fffcd56cd9c3c43e5f5cfe3e8e", "d35987d9292ee836f1442c65e645990cfbf94c83", "d4d3df14285e559c92d5294b04be97ebb26517fd", "df712b6b7a1081245929d2dfb70dc31ec1bf298c", "e197f5de3f970cc4ccc5d5b9b359bd21e9387c02", "e241a6cc646ce03d7255d5f370dca0c2539eeb95", "e5a4306b633a1b82eb1559ca1d91f62753abcd67", "e710338a9490adbcfd0519d8dbfe1d3b34cb4ab9", "e7ad5d2a422720eeae4a407456a5bfcc64755471", "ebd84317d8401f718867ae7f78410a6abd6b2c65", "ec3341e7adb89889deadc1d3ecd8d8a181b958f1", "f1939795edc045c213ed6dcc1f43195b65b675a2", "f49de5b9ac20ffed0144ad2bbbc249faa2c1fe91", "fd63390bf7f54266981f23de9d0628165917a21f"}),
            entry("react", new String[]{"036ae3c6e2f056adffc31dfb78d1b6f0c63272f0", "0470854f5522dde521d46049bcac894b2e86d280", "0887c7d56cb9b83f36dcb490b4245d7bc33bda1f", "08ff3d749d353a5ef5daf9fea78c456c5ebd048e", "0deea326674077598e351803d7a204a1c744a578", "1c2876d5b558b8591feb335d8d7204bc46f7da8a", "1d220ce0b7bc7f4843cf90f1fe8933b9fcba9deb", "24a83a5eeb1ccf4da1bdd97166d6c7c94d821bd8", "29e8924c70856bef9b11e0c74a450140bfcce773", "2f7bca0eb2487955e71a45e288e5847b5af522a5", "30bc8ef79295c71f5a4fc459c5bbd4b271f6f390", "313611572b6567d229367ed20ff63d1bca8610bb", "323efbc33c27a602a4aab8519f58feba1e0a216c", "366600d0b2b99ece8cd03d60e2a5454a02857502", "36c29393720157a3966ce1d50449a33a35bdf14c", "3766a014ae7752be70ca12b287637d8960dee6c3", "392530104c00c25074ce38e1f7e1dd363018c7ce", "3d8f465d99ece19238ccb561cdb157d2d676dda4", "41f920e43043ce027970cfe3e1ac65bbd2477c7a", "45b90d48668dae48aad611819b891d029aa2fb27", "47783e878d62ed96ea27290d1cdbb90b83c417c4", "48e54da484bc3416572fb48bc22d748a60ecd54f", "4a20ff26ecfe9bc66941d79f7fce2c67be8ee236", "4c3470eef832d64e03d18c19a70f2501f9becbfd", "4ca7855ca062d5d7dfca83c86acf46731e1e57ef", "4d6540893809cbecb5d7490a77ec7ad32e2aeeb3", "4f459bb144cad7c12e49d8afa2449ff6d9ed9e1f", "51e3f498a20572ecd5360c0460da212903a089bb", "5c6a496d98b80d19d851c2ec3a56d760b7118a50", "5cd5f63a77ac01e5418bc51ed81847ca3dcdb658", "5dfbfe9da740398c0a2cf4d897a0085000d06b7b", "6144212a8634948faf18cce8211c71e6f9d0667e", "6294b67a406d21cc6b65162e47497c1e8afe398f", "63aa7259b9f48886af545afcc06c29acf225b05f", "6c885d28c51ea30af0d8a4031dedcea98ef4114c", "6e258c126696bf50e8df98ae96626f58e54753ef", "71f591501b639c4adf329e1f586c7e04875dde7f", "73265fc478695cd80fefd035517b9ceef43d5f1d", "7b2d9655da218f8311d1dab4ab1142c35c3eef3b", "808f31af5cc571880058281c3ba1e1c69ce8aa5c", "83f76e4db9a060911774f497b5715bd4d4f42a9c", "8b83ea02f5abc7bf526b7e104a2cb73b034df5c2", "8c20615b06d22e023ff54a30e2602f0409876441", "8ec0e4a99df76c0ff1779cac4f2eaaaf35a6b5bb", "8fbcd499bd07c8a8206f23fe0b2a1cbbc1ffaf0a", "945fc1bfce5f2a93ac6ff0c53da3fd57e81b5a63", "999b656ed1c94b00fcfd043f54e18ade7553dee0", "9bd4d1fae21a6521c185cb114a15ca5dc74d6d9b", "9bda7b28f3b149c8f8ca826f0c395a81ed2d3bec", "9d310e0bc7b9d5ce39d82536dfcb67f98462a346", "a4cef2970341c08e5c16a2406fbf532fc8053d12", "b4b21486aa043b3c6260665930e7e638c908a5ec", "b5334a44e99c32ca26f7d65ca8312e13f7b7f4b7", "b5e961508709ac61f7e2de60a047238216f09a04", "b6e0512a81524d397ff4fbfb892372ecc84c6b02", "b99d0b14160150c566e091bd10b634beec9a58c3", "c040bcbea8e4393fbab549cc195162ac183625ed", "c0fe8d6f6942f5cbc93c09825e803ba8cf950522", "c601f7a64640290af85c9f0e33c78480656b46bc", "cda9fb04999bb0cc2dd148449748556464f3bf74", "cf58f296e9024fa76a32a53d7b3572c74b608ed3", "d289d4b634749861199556e42174a3f4a3ce2b16", "d480782c4162431d06c077ebf8fdf6b8ba7896ef", "d8d797645caebebda68971c93952ee812e636b1c", "d9869a45616884512220e162d1d3589f48a26fb0", "e0c78344e2f49fb91a75607ccdb170b5649bb1e9", "e0ca51a85d7c5b01c1efa7edb40080770d508cad", "e1ff342bf7f451fb995e0ea2bfb10889deef022f", "e96dc140599363029bd05565d58bcd4a432db370", "ef294ed6fcb7378ba25525e58e0588e3e8e85c48", "ef8d6d92a28835a8ab89a876f877306a5c3feffe", "f35d989bea2ee38c164c0cbb23cd300552e998f9", "f72043a36903a18703168a1ebfe271c150f27330", "fe10b8d0cd43e4bad998884c9ca61022091f7633"}),
            entry("react-native", new String[]{"0125813f213662ec1d9bb4a456f5671adcff9d83", "06c05e744d8af9582bde348210f254d76dae48b9", "166264d63b28582e31d083fab96ef46bc02ea469", "1d6ce2311f6a51821b33c5473414d70c8bd34425", "2f4ca831bfd936be9ae71a8a5700003abb65a0f5", "369e30f6859bc6af2a97670217ba142691bd49d9", "4435f087713e1d0ac3639e3b3196d71c6402898e", "488a4c7e1c86ac5900ff9194106511fbf5a8e3cb", "5259450c143f71c65e157d6b7d3f0e1655eb7aa1", "57daad98f01b59fce9cb9bf663fd0b191c56b232", "5d4c542c58d84bbe05f76bf01d9efdd9d438572c", "7014a30baae0b01378740113d15e4a7d721d916a", "83169c95a3525fcb74e34853022475093b9a1a77", "95e8592dcb9b50772336d2801f46305fdc95250d", "970caa4552d4ba87c1a954391535ff42b00832e7", "9fbff62b5ff9b3effb18f135e1cd9bd34a83c985", "a50b4ea7b9ecc4c95a38b58f12a71b93ff3a3131", "a90d0e3614c467c33cf85bcbe65be71903d5aecc", "ad67f556fb145d38fe547e1e8baa73ccc7a431b0", "b549e364e0025e0e1b4005f04a9de2d767006da1", "be32cbef007dada23bf830b3b12df8beefbbdac6", "c749d951ada829c6f6fb76f35e68142e61054433", "c948ae81686e2345d155577b4ff9f43f50021e99", "d1336ab16efc26449509b938dd46cf606d3caf34", "e708010d18f938e2d6b6424cfc9485d8e5dd2800", "f59e5a8d28491c64e4d0c08fec3a2b0b3fabb38b", "f6da9e1a9a932d2775cc998469d2f9b4ebd858c5", "f80000b9e72596392567a3deda59441e43f6327d", "f8c8231706492b588331354d45b833aa21434e13", "fbd1beaf666be9c09a380784f8c0cd34ba083a6b"}),
            entry("redux", new String[]{"27d9a24d1020685bc723f91ef60d0907df58ec23", "6baa290b57d455af7d8c2a2318cd7c0aab6dacfd", "7e745ce543baecb835173220e7bb51f17b19c875", "89b0c254ad806c9707fe8f2d3cd78b191b30d2c6", "8e82c15f1288a0a5c5c886ffd87e7e73dc0103e1", "99d47a55d769c456ac902f6738f9a90627d7cc8f", "acc10fac4bd381cbf143f9488ac886ed1475b19a", "b1c6569407f77d8cb7f27b8548eefee53a5b9634", "c4e6c3228e9cb8bd37d12756ec9049acc65007e1", "c6c0d697393b67147923f556a212465f6140d495", "c9ad6a40fe501919d359b910ea7c7f34532e11d9", "dd165dfc6878bc9aa6045bc1fc1943640a23e5e8", "e238a19767c750a49b5f94f8439c25ac04511c80", "e2e9648b264224af68add35431898dafe26b0a09", "e5e608eb87f84d4c6ec22b3b4e59338d234904d5", "fa3b2e4b56aa522de99d8848a0c13ea6fa860541"}),
            entry("reveal.js", new String[]{"0338f280d3942094782da57ea3b5b72bdb833e9d", "0563835fc00ce2c255cad0e4a089f559cdd48964", "0d37757f3f34dff5840eff6bd5ac6735938234a6", "25c46ccc37f932c04d5c2af7de1d12c6cdaed055", "50f9896362e373375d05eb3278ccab983849d3d9", "520fa4986eba954cba5d3a0ffa6f3697edba5047", "5f90a449cf1fa6edc93aaa4e69e6152c6096cd2e", "695293145193ed84b7ef193b538bb144ecb2832e", "90b301d0a03b8b23ce755c31ec6dac15cf69dc49", "91c6db71ca4de1878f1132f135a46d729e1b6960", "a03e8036289a49f0b4e0b43b1d8547f9ee439d42", "ab33b31f0a2892852903785893a4fa68862aa73d", "af270a909cdbf0de6edd25dfac4aeae0a58e8ab7", "bede9a22e86bb1c8debb945ccdfb22265393297d", "dfb39b4c96d86ebabc63bcae22a74afa99af66ca", "e29c706533c227682cfde1ac0b187e90738b9bbc", "e3a3d3aa0aa1806e20bffb0ff4c307ec6cc89964", "ef9cbbbbb92560f1ffd41dce23a584474922fe16", "fab28362ce2f46929c41cc1252281a7757b4695f"}),
            entry("Semantic-UI", new String[]{"0fa58217adef109e22faa3722d2d4f30bef71ac5", "1b48f527eb73d6bc4b1af2e94d52f51c32cec3c3", "27ff17b55b2c3af03bf3c1fa7449a44a40f7bafe", "2c26746dcedcc1610ab2c4ce0b64c218ab324547", "3b802c33c1aeb7473637e0362f26670c53158d3f", "6bc1c5ec4bdaf349e8ac5877f9fee0139a138d9d", "775df0e06179e5330a684379eb8b604718e41ed5", "789d020a3996e3d52fdb5206b159fc88762b3dac", "7e37d4a098a51c0a888ca273b07d1423e21eef7c", "8b38f91c51ab112897b2e4e63e5acd6f85cbdd8f", "9f1f6ab4cda3742e472a809dae71a2555a2ce354", "aa0c9ce453519bd339ea9adfd61f8763a0336c82", "bd6e37c5d0b1384e60ba552c9e517a1e938d26b6", "bef9b4632b91304d39d6d2ef0b2e0eea7c92790b", "edda7f888d6457d796b3c06a708355885698eb6f", "f725b162e70896e38257965424ac7f9af486b927"}),
            entry("socket.io", new String[]{"2b1a5afe6c3a07ee8f63118775b1af339a3b67d2", "3aa3213b13e914a668a76bc5eab1cef80708bf01", "3b5f4339a7b73c449684707f021c3f4aa2e01b13", "44a79f9cee892f607d038a9949243f91a2aa31e6", "463d7a16a139981f1eead005b388f720be0989bd", "5a123beea597c9fda7b722f18343fdc2c2755e79", "852a9d34dfae0fa9344805105511cb18d1e375a3", "8a3781499d3c88cdcafb90fbcd762e04da5552f0", "94df7bcdfd5696c63aff496d850ff0305b019e67", "c0c79f019e7138194e438339f8192705957c8ec3", "d286ba2064e516c4643e48222b60af404aad31e4", "fc34c7d7a489a37a83de2e2af6ffcc41351110fa"}),
            entry("three.js", new String[]{"01b263d3362f8bab63dc26b04e4a7ac1fb1d2641", "06903c44a2d9c8cb0a57373999e0596aa51d5627", "06ba5ebad10fc6d7192b0c522fc4941c2807d416", "27a5b9a172a532c54e9ad75bdaa7434652409198", "2a808c6e455c5053536a3caf7a9dac48dee13c03", "35ae830a7c4544582ed2759e5b18c5d6ef37c6d9", "5a1fb62e49d796cff22e1bf848f363aaedbef9fe", "5ed9d6cf803c4283c948c6c47ff278f6ecb22d0d", "6d916aed6b31f4d860efb52f6b94e3d3ce49d1ba", "73f083710d64acb493f55ba2c07e24c5a7f62899", "7f0a1234c2d6726f625740595455ff5274f7d5f6", "847ecb4d678f9f40b31dd7139bc40b2cffa34830", "8e8589c88105b4f9258867f61aadd70c9fccffaa", "99d5b58a771fc09377d7393e913fc641f5d9aece", "a4b52f8cb5569c69d3801e5b4ba236cd75fdfde9", "a879d8d05be150ba3f2790be6212d92215558269", "b55374012d1842c3c536b757adc7ad9ff5caa68d", "ba4489ded66212ac9e6d3017a6bb856023bd026f", "c209347254eff973802088c7e4e336490db2597e", "c9bb04c1b000b02c153b71ace001b9cae5872fcc", "ca803d97c0e4fab6eadd3f745f3068443e8ca1f4", "cb9dc432ac2e4c2fb586f3131ce2a0e1376a28a1", "cc3d60f8d860326264ca7416b25ffca5386a9a27", "e6c13503ac9a467f78bfe39f7a2c8fe4219308ec", "f0e7bdc1de54a1b896089d819872111a86aa4185", "f3f33b5c5a661c04062cd0b8bf98f74d85e2abb7"}),
            entry("vue", new String[]{"015a31890250e93564959de97372be7bd6195f62", "050bb33f9b02589357c037623ea8cbf8ff13555b", "09106f066a1ba71431e4f9f26246aaf619153e2e", "0c11aa8addf5ad852e37da358ce2887af72e4193", "0c9534ff0069b5289ea9598bcb4f5e5ac346c979", "144a4dd860b20ca48263bac150286f627e08d953", "1a979c44d6543d89f8a7e26ad7f995b1bf2aee3c", "1c8e2e88ed2d74a02178217b318564b73a096c18", "1dd6b6f046c3093950e599ccc6bbe7a393b8a494", "215f877d1b7eb6583f7adf15676ead8611f07379", "230c6ae7822347b9b2a659503291e45fcc58fe41", "2b3e1a0a964c21d405f67479a9960c2d00f235b4", "2b5c83af6d8b15510424af4877d58c261ea02e16", "2e0f6d5d817957ab23819f90b264243d87ef968c", "318f29fcdf3372ff57a09be6d1dc595d14c92e70", "3932a451a1419a97ea0200c5cb8096afe9a3e7e7", "3b32652aa68a06d881e3149bb21ac8711887e7f6", "3d14e855e422b656859d1b419af43b94320fcfce", "3ee62fd59e20030dd63c08c2390e803d034928fe", "41d774d112946f986bf0b0e3f30fd962c01ceba2", "46c8016562c3a9308fcf45d31a5a674312e2c110", "4d8226fb2c84fa2e13a2d8a86dea8a9a5c6ea95f", "4e0c48511d49f331fde31fc87b6ca428330f32d1", "50b711af43708426e63b4ea529436b49fafc3f2e", "514b90b64770cba9f905d2dff59dfa0e064e580c", "55a719c93aeffb8176fcc7193aa3a813cac3099d", "5db86b4e94857fdde3ae6b71e24da637bc116baa", "60da366a2653a3984d79331d02ebb2ecf7e73a9a", "62265035c0c400ad6ec213541dd7cca58dd71f6e", "62e47c9eb4446da79d66ad2385c199f31b4348d8", "644274cbd34e14e74e8931fa979b22dc2db04895", "653aac2c57d15f0e93a2c1cc7e6fad156658df19", "679cd1fef448989bf645313c391e4134ecd9f593", "68934997444c0047c49e419761dfad7fbc043a5d", "6bc75cacb72c0cc7f3d1041b5d9ff447ac2f5f69", "6d6b3739e132723915bc2209663db1b825307865", "6dac3dbe441302cebb945b675f78f8e7247e2a97", "711aaf71bb126fb5a0dd3ab032a7793a74918f02", "7ad368ebb6987bd4044f9df184d73ce14ca680f2", "8335217cb4bd13fb07e08a76c07df0fceed6c197", "882e7199fd8eee039291c4b9f7f324dcf46f32fd", "88423fc66a2a4917dcdb7631a4594f05446283b1", "88f3889f19678981944339be8d22c3ebcd11f822", "8936b8d9c147441555fcfd4ac748d817ba5ff60e", "8b893c13d6ffa79f294fec76a228509ec48e4706", "90891709708e8ebdc1522eed678a49eb9f312fda", "90ed48224e0ae281a2579b997e4bd5a150b80413", "956756b1be7084daf8b6afb92ac0da7c24cde2a5", "984927a1a98d10ad8af44f2accfb08d34d517610", "9bded22a83b6fb9a89a32009e7f47f6201e167a3", "9edcc6b6c7612efc15e4bfc5079279533190a2f2", "a08feed8c410b89fa049fdbd6b9459e2d858e912", "a23b913796a7d18e76185607f250655e18a390c8", "a32490f27dbc2994c456e2c9ab183630d3b42a21", "a43d66743be2bd62b2398090663e41eeaf0dc75f", "ae07fedf8ab00a000db56a155f2e2fdaa6daeff2", "b0f00e31e7d06edfdc733e2e7f24d5ca448759f9", "b3cd9bc3940eb1e01da7081450929557d9c1651e", "b5cc8b9e935897184ac43119bf5fbeca606f4b6a", "b60964256c876de2516977c776201ef56ab13fb7", "bc2918f0e596d0e133a25606cbb66075402ce6c3", "bc719405c084afb5dd4168413d880a4b801e234f", "c104cc582d647f5e10b90563cb80907b9e30ec12", "cf1ff5b0dc3d15c1e16821cb5e4fc984c74f07c1", "d6e6f1deb180a4f47e94496724623b9e6d8e08b3", "dae173d96d15f47de6ce6961354d5c05e4273005", "dbf15103f797a54f41288af2a393b1e8ccee0aec", "dc2171a33a38d0563ef14934b078d9dc4c39acf3", "df82aeb0bf7454ac99d403000a1ac993e8d8d4de", "e9ea565d915cdffbad17bb78aa30af752dd4e5e6", "ea0d227d2ddfa5fc5e1112acf9cd485b4eae62cb", "ef432c6e86d34d891034fd7668a85793be4a2607", "f3fe012d5499f607656b152ce5fcb506c641f9f4", "fd0c4f5a6c9324a4943fe6e7f47bde8a05b4996d"}),
            entry("webpack", new String[]{"17eb5b47009b925901f6489df77ba1092d5d254b", "1e4b1c72125e826aba32e99d62464db4613ed64f", "1e4d2b7fe71a2633df6816b82411402136064159", "214801493ed33f3ee91215f45c38408327516a86", "21e1e6f8d5ec994d1b5583b524278ff33c573ada", "35130585ae6a7a346234c27ba379a4280006452c", "42c0214254df5ade315daf4a51580f0424a1792f", "4b1a76bdb00ebf89b977c2cc1199870d0c180c72", "53103a9690d653daf1de405756f5638999c36f22", "53d26bfc963c34dd26c7eba44facdea1d5081991", "5d05136904e74bfef9d14892b403706c44f44096", "5da9d8c7ef29f954a37f58f5138f116579c6efe8", "64925a80c7f13302b17b62e5fe581505d3ab08eb", "6b583e375103a66ce7de857dced19f572f620f48", "6d8bc91a9b32b492d40fd358c964ed66516dd518", "756f2ca1779fd9836412041cbc9baa7912d490ae", "78b31936c3baf7dc0e71f10189ddf415d22c2762", "82a71be1dcb14e057637ef6d558ee7a53516717b", "8636670169131f0617713eb012c41f2c04413430", "86aa18d6990f4102704854ad5dc76b1cc45323f0", "86c00207bdc9cb1ef60441d1ec836624a162c9ab", "8b3772d47fc94fe3c3175602bba5eef6605fad86", "9156be961d890b9877ddef3a70964c9665662abb", "9b37c6bf2c40ede496de8c2720eb3f93bfc29e62", "9c7100ba6023347cd2ded17ec16900753b01c18d", "9cb1a663173f5fcbda83b2916e0f1679c1dc642e", "b50d4cf7c370dc0f9fa2c39ea0e73e28ca8918ac", "b642403d86caa9f5d4d085135ad2e40059bd8bfe", "c47150c42c980b297c747a17d79bd6722a65fb84", "d91e7ecafc5f52a27463db031c114a71f29c3b0a", "da6f869c1ef39abd0f1e512b07f41f6bbecd4b88", "dccd8624ed7f3f73e33b6430470e8a3bc3db0c4d", "e4836826373151c8d14db011a1c3f2763aeb727a", "e6562319fecf9ce5230876c504bfa495ce3bdabd", "eefacf3f2a8bdb5506163932138786a03985390f", "f613e9ac2e6de4e8f767e2268ca7dd636ac3aa9d", "f65a24d9dbc7a3642b1f019b0ed37216b83438d1"})
    );
}
