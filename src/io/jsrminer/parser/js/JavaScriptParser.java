package io.jsrminer.parser.js;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.api.IParser;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaScriptParser implements IParser {

    @Override
    public UMLModel parse(Map<String, String> fileContents) {
        final HashMap<String, FunctionDeclaration[]> fds = new HashMap<>();
        final UMLModel umlModel = new UMLModel();

        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
            jsEngine.createParseFunction();

            for (String filepath : fileContents.keySet()) {
                final String content = fileContents.get(filepath);
                final V8Array fdsArray = processScript(content, jsEngine);
                final FunctionDeclaration[] fd = convert(fdsArray, filepath);
                fds.put(filepath, fd);
                fdsArray.release();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        umlModel.setFileFunctionDeclarations(fds);
        return umlModel;
    }

    private FunctionDeclaration[] convert(final V8Array fdsArray, String file) {
        final FunctionDeclaration[] fds = new FunctionDeclaration[fdsArray.length()];

        FunctionDeclaration fd;
        SourceLocation location;
        String qualifiedName;
        String body;
        V8Array v8ParamsArray;

        for (int i = 0; i < fds.length; i++) {
            // Extract nodes
            V8Object v8Fd = fdsArray.getObject(i);
            V8Object v8Location = v8Fd.getObject("location");

            // Extract fds info
            qualifiedName = v8Fd.getString("qualifiedName");
            body = v8Fd.getString("body");
            v8ParamsArray = v8Fd.getArray("params");

            // Create java object
            fd = new FunctionDeclaration(qualifiedName, true);
            fd.setParameters(JV8.toStringArray(v8ParamsArray));

            location = JV8.parseLocation(v8Location);
            location.setFile(file);
            fd.setSourceLocation(location);
            fd.setBody(new FunctionBody(body));

            fds[i] = fd;
            v8Fd.release();
            v8Location.release();
            v8ParamsArray.release();
        }

        fdsArray.release();
        return fds;
    }

    //@JsonCreator
    public static BlockStatement fromJson(String blockStatementJson) {
        BlockStatement block = new BlockStatement();
        Any any = JsonIterator.deserialize(blockStatementJson);

        // Parse source location
        SourceLocation location = any.get("loc").as(SourceLocation.class);
        block.setSourceLocation(location);

        // Parse the nested statements
        List<Any> statements = any.get("statements").asList();
        for (Any statement: statements) {
            String type = statement.get("type").toString();
            boolean isComposite =  "BlockStatement".equals(type);

            if (isComposite) {
                // TO Do a block statement again
            }else {
                // A leaf statement
                SingleStatement singleStatement = SingleStatement.fromJson(statement.toString());
                block.addStatement(singleStatement);
            }
        }

        return block;
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