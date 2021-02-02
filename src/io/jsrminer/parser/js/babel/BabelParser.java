package io.jsrminer.parser.js.babel;

import io.jsrminer.parser.JsonFileLoader;
import io.jsrminer.parser.js.JavaScriptParser;
import io.jsrminer.uml.UMLModel;
import io.rminerx.core.api.ISourceFile;
import io.rminerx.core.entities.SourceFile;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jgit.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BabelParser extends JavaScriptParser {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String SCRIPTS_DIRECTORY_NAME = "src-js/scripts";

    @Override
    public UMLModel parse(Map<String, String> fileContents) {
        final HashMap<String, ISourceFile> sourceModels = new LinkedHashMap<>();
        final UMLModel umlModel = new UMLModel();

        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
            jsEngine.createParseFunction();

            for (String filepath : fileContents.keySet()) {
                final String content = fileContents.get(filepath);

                try {
                    log.info("Processing " + filepath + "...");
                    SourceFile sourceFile = parse(content, jsEngine, filepath);
                    sourceFile.setFilepath(filepath);
                    sourceModels.put(filepath, sourceFile);
                } catch (Exception ex) {
                    System.out.println("Ignoring and removing file " + filepath + " due to exception" + ex.toString());
                    fileContents.remove(filepath);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        umlModel.setSourceFileModels(sourceModels);
        return umlModel;
    }

    @Override
    public ISourceFile parseSource(String content, @NonNull String filepath) {
        if (filepath == null)
            throw new NullPointerException("filepath cannot be null");

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
        final String blockJson = processScript(fileContent, jsEngine);
        StopWatch watch = new StopWatch();
        watch.start();
        SourceFile file = new JsonFileLoader(filePath).parseSourceFile(blockJson);
        watch.stop();
        log.debug("Model loading time from json: " + watch.toString());
        return file;
    }

    private String processScript(String script, JavaScriptEngine jsEngine) {
        try {
            return (String) jsEngine.executeFunction("parse", script, true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
