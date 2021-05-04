package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import io.jsrminer.parser.js.closurecompiler.INodeVisitor;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.entities.Container;
import io.rminerx.core.entities.SourceFile;

import java.util.EnumMap;
import java.util.Map;

import static com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType.FUNCTION_DECLARATION;
import static java.util.Map.entry;

public class Visitor {
    private final String filename;
    private final String fileContent;

    public Visitor(String filename, String fileContent) {
        this.filename = filename;
        this.fileContent = fileContent;
    }

    final BabelNodeVisitor<VariableDeclaration, ICodeFragment> processVariableDeclaration
            = (JV8 node, ICodeFragment parent, IContainer container) -> {
        String kindStr = node.get("kind").asString();
        var kind = VariableDeclarationKind.fromName(kindStr);
        return null;
    };

    private Map<String, BabelNodeVisitor<?, ICodeFragment>> nodeProcessors = Map.ofEntries(
            entry("VariableDeclaration", processVariableDeclaration)
//            entry("VariableDeclaration", processIdentifier)
    );

    public void visit(JV8 node, NodePath path) {
        final JV8 elementType = node.get("type");
        String type = elementType.asString();
        nodeProcessors.get(type).visit(node, path.getParent(), path.getContainer());
    }

    public SourceLocation createSourceLocation(JV8 node) {
        int start = node.get("start").asInt();
        int end = node.get("end").asInt();

        JV8 loc = node.get("loc");
        JV8 startLoc = loc.get("start");
        JV8 endLoc = loc.get("end");
        int startLine = startLoc.get("line").asInt();
        int startColumn = startLoc.get("column").asInt();
        int endLine = endLoc.get("line").asInt();
        int endColumn = endLoc.get("column").asInt();

//        loc.close();
//        startLoc.close();
//        endLoc.close();

        return new SourceLocation(
                this.filename
                , startLine
                , startColumn
                , endLine
                , endColumn
                , start
                , end
        );
    }

    public SourceFile loadFromAst(JV8 programAST) {
        var container = new SourceFile(filename);
        container.setSourceLocation(createSourceLocation(programAST));

        BlockStatement dummyBodyBlock = new BlockStatement();
        dummyBodyBlock.setText("");
        dummyBodyBlock.setSourceLocation(container.getSourceLocation());
        var path = new NodePath(container, dummyBodyBlock);
        JV8 body = programAST.get("body");
        for (int i = 0; i < body.size(); i++) {
            var member = body.get(i);
            visit(member, path);
        }

        container.getStatements().addAll(dummyBodyBlock.getStatements());

        return container;
    }
}
