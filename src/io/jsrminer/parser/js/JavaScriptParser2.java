package io.jsrminer.parser.js;

import com.eclipsesource.v8.V8Array;
import io.jsrminer.api.IParser;
import io.jsrminer.sourcetree.SourceFileModel;
import io.jsrminer.uml.UMLModel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaScriptParser2 implements IParser {
    public static final String SCRIPTS_DIRECTORY_NAME = "src-js/scripts";

    @Override
    public UMLModel parse(Map<String, String> fileContents) {
        final HashMap<String, SourceFileModel> sourceModels = new LinkedHashMap<>();
        final UMLModel umlModel = new UMLModel();

        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
            jsEngine.createParseFunction();

            for (String filepath : fileContents.keySet()) {
                final String content = fileContents.get(filepath);

                SourceFileModel source = loadSourceFileModel(content, jsEngine, filepath);
                sourceModels.put(filepath, source);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        umlModel.setSourceFileModels(sourceModels);
        return umlModel;
    }

    private SourceFileModel loadSourceFileModel(String fileContent, JavaScriptEngine jsEngine, String filePath) {
        final SourceFileModel source = new SourceFileModel(filePath);

        return source;
    }

    private V8Array processScript(String script, JavaScriptEngine jsEngine) {
        // String json = null;
        try {
            //json = (String) jsEngine.executeFunction("parse", script);
            return (V8Array) jsEngine.executeFunction("parse", script);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
