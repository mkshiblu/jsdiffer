package io.jsrminer.uml;

import io.jsrminer.parser.js.closurecompiler.ClosureCompilerParser;
import org.apache.commons.io.FilenameUtils;

import java.util.Map;
import java.util.Set;

public class UMLModelFactory {
//    public static UMLModel createUMLModel(Map<String, String> fileContents, Set<String> repositoryDirectories) {
//        JavaScriptParser parser = new JavaScriptParser();
//        return parser.parse(fileContents/*, repositoryDirectories*/);
//    }

    public static UMLModel createUMLModel(Map<String, String> fileContents) {
        ClosureCompilerParser parser = new ClosureCompilerParser();
        parser.setEnableStrictMode(true);
        UMLModel model = parser.parse(fileContents);

        // Populate repository directories
        for (String path : fileContents.keySet()) {
//            String directory = path;
//            while (directory.contains(File.separator)) {
//                directory = directory.substring(0, directory.lastIndexOf("/"));
//                //umlModel.repositoryDirectories.add(directory);
//            }
            allDirectoriesInPath(path, model.repositoryDirectories);
        }

        return model;
    }

    public static void allDirectoriesInPath(String filepath, Set<String> directories) {
        String name = FilenameUtils.getName(filepath);
        String dir = filepath.substring(0, filepath.length() - name.length());
        if (dir.length() > 0) {
            dir = dir.substring(0, dir.length() - 1);
            directories.add(dir);
            allDirectoriesInPath(dir, directories);
        }
    }
}
