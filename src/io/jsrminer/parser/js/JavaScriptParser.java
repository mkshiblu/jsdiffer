package io.jsrminer.parser.js;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import io.jsrminer.api.IParser;
import io.jsrminer.sourcetree.FunctionBody;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceFileModel;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.uml.UMLModel;
import io.jsrminer.uml.UMLParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaScriptParser implements IParser {
    public static final String SCRIPTS_DIRECTORY_NAME = "src-js/scripts";

    @Override
    public UMLModel parse(Map<String, String> fileContents) {
        final HashMap<String, SourceFileModel> sourceModels = new LinkedHashMap<>();
        final UMLModel umlModel = new UMLModel();

        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
            jsEngine.createParseFunction();

            for (String filepath : fileContents.keySet()) {
                final String content = fileContents.get(filepath);

                SourceFileModel source = parse(content, jsEngine, filepath);
                sourceModels.put(filepath, source);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        umlModel.setSourceFileModels(sourceModels);
        return umlModel;
    }

    public SourceFileModel parseSource(String content) {
        String filepath = null;
        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
            jsEngine.createParseFunction();
            return parse(content, jsEngine, filepath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SourceFileModel parse(String fileContent, JavaScriptEngine jsEngine, String filepath) {
        final V8Array fdsArray = processScript(fileContent, jsEngine);
        final FunctionDeclaration[] fds = convert(jsEngine, fdsArray, filepath);
        // Create source model
        final SourceFileModel source = new SourceFileModel(filepath);
        source.setFunctionDeclarations(fds);
        fdsArray.release();
        return source;
    }

    private FunctionDeclaration[] convert(JavaScriptEngine jsEngine, final V8Array fdsArray, String file) {
        final FunctionDeclaration[] fds = new FunctionDeclaration[fdsArray.length()];

        FunctionDeclaration fd;
        SourceLocation location;
        String qualifiedName;
        String body;
        V8Array v8ParamsArray;
        V8Object v8Body;

        for (int i = 0; i < fds.length; i++) {
            // Extract nodes
            V8Object v8Fd = fdsArray.getObject(i);
            V8Object v8Location = v8Fd.getObject("location");

            // Extract fds info
            qualifiedName = v8Fd.getString("qualifiedName");


            v8ParamsArray = v8Fd.getArray("params");

            // Create java object
            fd = new FunctionDeclaration(qualifiedName, true);
            fd.setParameters(convertToUMLParameters(v8ParamsArray));

            location = JV8.parseLocation(v8Location);
            location.setFile(file);
            fd.setSourceLocation(location);

            v8Body = v8Fd.getObject("body");
            body = jsEngine.toJson(v8Body);
            fd.setBody(new FunctionBody(body));

            fds[i] = fd;
            v8Body.release();
            v8Fd.release();
            v8Location.release();
            v8ParamsArray.release();
        }

        fdsArray.release();
        return fds;
    }

    UMLParameter[] convertToUMLParameters(final V8Array v8ParamsArray) {
        final UMLParameter[] params = new UMLParameter[v8ParamsArray.length()];
        String name;
        for (int i = 0; i < params.length; i++) {
            name = v8ParamsArray.getString(i);
            params[i] = new UMLParameter(name);
        }
        v8ParamsArray.release();
        return params;
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
