package io.jsrminer.parser;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.uml.UMLModel;

import java.util.HashMap;
import java.util.Map;

public class JavaScriptParser {

    public UMLModel parse(Map<String, String> fileContents) {
        final HashMap<String, FunctionDeclaration[]> fds = new HashMap<>();
        final UMLModel umlModel = new UMLModel();

        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
            jsEngine.createParseFunction();

            for (String filepath : fileContents.keySet()) {
                final String content = fileContents.get(filepath);
                final V8Array fdsArray = processScript(content, jsEngine);
                final FunctionDeclaration[] fd = covert(fdsArray, filepath);
                fds.put(filepath, fd);
                fdsArray.release();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        umlModel.setFunctionDeclarations(fds);
        return umlModel;
    }

    private FunctionDeclaration[] covert(final V8Array fdsArray, String file) {
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
            fd = new FunctionDeclaration(qualifiedName);
            fd.setParameters(JV8.toStringArray(v8ParamsArray));

            location = JV8.parseLocation(v8Location);
            location.setFile(file);
            fd.setLocation(location);
            fd.setBody(body);

            fds[i] = fd;
            v8Fd.release();
            v8Location.release();
            v8ParamsArray.release();
        }

        fdsArray.release();
        return fds;
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

//    private void getCst(CstRoot root, SourceFile sourceFile, String content, SourceFileSet sources) throws Exception {
//        try {
//            V8Object babelAst = (V8Object) this.nodeJs.getRuntime().executeJSFunction("parse", content);
//
//            // System.out.print(String.format("Parsing %s ... ", sources.describeLocation(sourceFile)));
//            // long timestamp = System.currentTimeMillis();
//            try (JsValueV8 astRoot = new JsValueV8(babelAst, this::toJson)) {
//
//                TokenizedSource tokenizedSource = buildTokenizedSourceFromAst(sourceFile, astRoot);
//                root.addTokenizedFile(tokenizedSource);
//
//                // System.out.println(String.format("Done in %d ms", System.currentTimeMillis() - timestamp));
//                Map<String, Set<CstNode>> callerMap = new HashMap<>();
//                getCst(0, root, sourceFile, content, astRoot, callerMap);
//
//                root.forEachNode((calleeNode, depth) -> {
//                    if (calleeNode.getType().equals(JsNodeType.FUNCTION) && callerMap.containsKey(calleeNode.getLocalName())) {
//                        Set<CstNode> callerNodes = callerMap.get(calleeNode.getLocalName());
//                        for (CstNode callerNode : callerNodes) {
//                            root.getRelationships().add(new CstNodeRelationship(CstNodeRelationshipType.USE, callerNode.getId(), calleeNode.getId()));
//                        }
//                    }
//                });
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(String.format("Error parsing %s: %s", sources.describeLocation(sourceFile), e.getMessage()), e);
//        }
//    }
}
