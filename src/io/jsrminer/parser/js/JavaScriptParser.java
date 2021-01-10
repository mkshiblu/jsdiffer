package io.jsrminer.parser.js;

import io.jsrminer.parser.JsonCompositeDeserializer;
import io.jsrminer.uml.UMLModel;
import io.rminer.core.api.IParser;
import io.rminer.core.api.ISourceFile;
import io.rminer.core.entities.SourceFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaScriptParser implements IParser {
    public static final String SCRIPTS_DIRECTORY_NAME = "src-js/scripts";

    @Override
    public UMLModel parse(Map<String, String> fileContents) {
        final HashMap<String, ISourceFile> sourceModels = new LinkedHashMap<>();
        final UMLModel umlModel = new UMLModel();

        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
            jsEngine.createParseFunction();

            for (String filepath : fileContents.keySet()) {
                final String content = fileContents.get(filepath);

                SourceFile sourceFile = parse(content, jsEngine, filepath);
                sourceFile.setFilepath(filepath);
                sourceModels.put(filepath, sourceFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        umlModel.setSourceFileModels(sourceModels);
        return umlModel;
    }

    @Override
    public ISourceFile parseSource(String content, String filepath) {
        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
            jsEngine.createParseFunction();

            SourceFile source = parse(content, jsEngine, filepath);
            source.setFilepath(filepath);
            return source;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the code using the jsEngine
     *
     * @return
     */
    private SourceFile parse(String fileContent, JavaScriptEngine jsEngine, String filePath) {
        // IComposite body = new CompositeFragment();
        final String blockJson = processScript(fileContent, jsEngine);
        return new JsonCompositeDeserializer(filePath).parseSourceFile(blockJson);
    }

    private String processScript(String script, JavaScriptEngine jsEngine) {
        try {
            return (String) jsEngine.executeFunction("parse", script, true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
