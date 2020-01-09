package io.jsrminer.parser;

import java.util.List;

import com.eclipsesource.v8.V8Array;

public class JsParser implements IParser {
    @Override
    public void parse(List<String> sourceFiles) {

    }

    /*private void getCst(CstRoot root, SourceFile sourceFile, String content, SourceFileSet sources) throws Exception {
        try {
            V8Object babelAst = (V8Object) this.nodeJs.getRuntime().executeJSFunction("parse", content);

            // System.out.print(String.format("Parsing %s ... ", sources.describeLocation(sourceFile)));
            // long timestamp = System.currentTimeMillis();
            try (JsValueV8 astRoot = new JsValueV8(babelAst, this::toJson)) {

                TokenizedSource tokenizedSource = buildTokenizedSourceFromAst(sourceFile, astRoot);
                root.addTokenizedFile(tokenizedSource);

                // System.out.println(String.format("Done in %d ms", System.currentTimeMillis() - timestamp));
                Map<String, Set<CstNode>> callerMap = new HashMap<>();
                getCst(0, root, sourceFile, content, astRoot, callerMap);

                root.forEachNode((calleeNode, depth) -> {
                    if (calleeNode.getType().equals(JsNodeType.FUNCTION) && callerMap.containsKey(calleeNode.getLocalName())) {
                        Set<CstNode> callerNodes = callerMap.get(calleeNode.getLocalName());
                        for (CstNode callerNode : callerNodes) {
                            root.getRelationships().add(new CstNodeRelationship(CstNodeRelationshipType.USE, callerNode.getId(), calleeNode.getId()));
                        }
                    }
                });
            }

        } catch (Exception e) {
            throw new RuntimeException(String.format("Error parsing %s: %s", sources.describeLocation(sourceFile), e.getMessage()), e);
        }
    }*/
}
