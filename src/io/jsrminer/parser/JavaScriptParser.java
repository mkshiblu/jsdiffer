package io.jsrminer.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.IntNode;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.UMLModel;

public class JavaScriptParser {

    public UMLModel parse(Map<String, String> fileContents) {
        JavaScriptEngine jsEngine = new JavaScriptEngine();
        jsEngine.createParseFunction();

        final HashMap<String, FunctionDeclaration> fds = new HashMap<>();

        for (String filepath : fileContents.keySet()) {
            final String content = fileContents.get(filepath);
            final String json = processScript(content, jsEngine);
            final FunctionDeclaration fd = convert(json);
            fds.put(filepath, fd);
        }
        return null;
    }

    private FunctionDeclaration convert(String json) {
        final String qualifiedName = json;
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(FunctionDeclaration.class, new StdDeserializer<FunctionDeclaration>((JavaType) null) {
            @Override
            public FunctionDeclaration deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                JsonNode node = jp.getCodec().readTree(jp);
                String qualifiedName = node.get("qualifiedName").asText();
                return new FunctionDeclaration(qualifiedName);
            }
        });
        mapper.registerModule(module);

        try {
            FunctionDeclaration fd = mapper.readValue(json, FunctionDeclaration.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private String processScript(String script, JavaScriptEngine jsEngine) {
        String json = null;
        try {
            // Json the whole program currently let's say its just the fds
            //json = (String) jsEngine.executeFunction("parse", script);
            V8Array fdsArray = (V8Array) jsEngine.executeFunction("parse", script);
            String qualifiedName;
            String body;

            int len = fdsArray.length();
            for (int i = 0; i < len; i++) {
                V8Object fd = (V8Object) fdsArray.get(i);
                qualifiedName = (String) fd.get("qualifiedName");
                fd.release();
            }

            fdsArray.release();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return json;
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
