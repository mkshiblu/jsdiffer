package io.jsrminer.uml;

import com.eclipsesource.v8.NodeJS;
import io.jsrminer.io.FileUtil;
import io.jsrminer.parser.js.UMDHandler;
import io.jsrminer.parser.js.babel.BabelParser;
import io.jsrminer.sourcetree.JsConfig;

import java.util.Map;

public class UMLModelBuilder {

    public static UMLModel createUMLModel(Map<String, String> fileContents) {
        var parser = new BabelParser();
        UMLModel model = parser.parse(fileContents);

        // Populate repository directories
        for (String path : fileContents.keySet()) {
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
