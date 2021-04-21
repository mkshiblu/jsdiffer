package io.jsrminer.uml;

import io.jsrminer.io.FileUtil;
import io.jsrminer.parser.js.JavaScriptParser;
import io.jsrminer.parser.js.UMDHandler;
import io.jsrminer.parser.js.babel.BabelParser;
import io.jsrminer.parser.js.closurecompiler.ClosureCompilerParser;
import io.jsrminer.sourcetree.JsConfig;

import java.util.Map;

public class UMLModelFactory {
//    public static UMLModel createUMLModel(Map<String, String> fileContents, Set<String> repositoryDirectories) {
//        JavaScriptParser parser = new JavaScriptParser();
//        return parser.parse(fileContents/*, repositoryDirectories*/);
//    }

    public static UMLModel createUMLModel(Map<String, String> fileContents) {
        var parser = new BabelParser();
        //parser.setEnableStrictMode(true);
        UMLModel model = parser.parse(fileContents);

        // Populate repository directories
        for (String path : fileContents.keySet()) {
//            String directory = path;
//            while (directory.contains(File.separator)) {
//                directory = directory.substring(0, directory.lastIndexOf("/"));
//                //umlModel.repositoryDirectories.add(directory);
//            }
            FileUtil.allDirectoriesInPath(path, model.getRepositoryDirectories());
        }

        // filter UMD if enabled
        if (JsConfig.treatUMDAsSourceFile) {
            UMDHandler umdHandler = new UMDHandler();
            for (var sourceFile : model.getSourceFileModels().values()) {
                if (umdHandler.isUMD(sourceFile))
                    umdHandler.hoistUMDCodeToSourceFileLevel(sourceFile);
            }
        }

        return model;
    }

}
