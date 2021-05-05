package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;
import io.rminerx.core.entities.SourceFile;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class Visitor {
    private final String filename;
    private final String fileContent;

    private Map<String, BabelNodeVisitor<Object, JV8, ICodeFragment>> nodeProcessors = new HashMap<>();

    public Visitor(String filename, String fileContent) {
        this.filename = filename;
        this.fileContent = fileContent;
        initProcessors();
    }

    void initProcessors() {
        nodeProcessors.put("VariableDeclaration", Declarations.returnStatementProcessor);
    }

    /**
     * interface VariableDeclaration <: Declaration {
     * type: "VariableDeclaration";
     * declarations: [ VariableDeclarator ];
     * kind: "var" | "let" | "const";
     * }
     */
    final BabelNodeVisitor<VariableDeclaration, JV8, ILeafFragment> processVariableDeclaration
            = (JV8 node, ILeafFragment parent, IContainer container) -> {
        String kindStr = node.get("kind").asString();
        var kind = VariableDeclarationKind.fromName(kindStr);
        var declarations = node.get("declarations");

        for (int i = 0; i < declarations.size(); i++) {
            processVariableDeclarator(declarations.get(i), kind, parent);
        }
        return null;
    };

    /**
     * interface VariableDeclarator <: Node {
     * type: "VariableDeclarator";
     * id: Pattern;
     * init: Expression | null;
     * }
     *
     * @param {declaratorPath} path
     */
    VariableDeclaration processVariableDeclarator(JV8 node, VariableDeclarationKind kind, ILeafFragment leaf) {
        String variableName = node.get("id").get("name").asString();
        VariableDeclaration variableDeclaration = createVariableDeclaration(node, variableName, kind, leaf.getParent());

        if (node.get("init") != null) {
            //        Expression initializer = astProcessor.processExpression(path.get("init"), statement);
            //      variableDeclaration.initializer = initializer;
        }

        return variableDeclaration;
    }

    VariableDeclaration createVariableDeclaration(JV8 node, String variableName
            , VariableDeclarationKind kind
            , INode scopeNode) {
        var variableDeclaration = new VariableDeclaration(variableName, kind);
        variableDeclaration.setSourceLocation(createSourceLocation(node));

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(createVariableScope(variableDeclaration.getSourceLocation(), scopeNode));

        return variableDeclaration;
    }

    static SourceLocation createVariableScope(SourceLocation variableLocation, INode scopeNode) {
        final SourceLocation parentLocation = scopeNode.getSourceLocation();
        return new SourceLocation(parentLocation.getFilePath(),
                variableLocation.startLine,
                variableLocation.startColumn,
                parentLocation.endLine,
                parentLocation.endColumn,
                variableLocation.start,
                parentLocation.end
        );
    }

    void visitExpression(JV8 node, ILeafFragment leaf, IContainer container) {
        visit(node, leaf, container);
    }

    Object visitStatement(JV8 node, BlockStatement parent, IContainer container) {
        return visit(node, parent, container);
    }

    Object visit(JV8 node, ICodeFragment parent, IContainer container) {
        final JV8 elementType = node.get("type");
        String type = elementType.asString();
        var processor = nodeProcessors.get(type);
        if (processor == null) {
            if (!isIgnored(type))
                throw new NotImplementedException("Processor not implemented for " + type);
        } else {
            Object result = processor.visit(node, parent, container);
            return result;
        }
        return null;
    }
//
//    public void visit(JV8 node, NodePath path) {
//        final JV8 elementType = node.get("type");
//        String type = elementType.asString();
//        nodeProcessors.get(type).visit(node, path.getParent(), path.getContainer());
//    }

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
            visit(member, path.getParent(), path.getContainer());
        }

        container.getStatements().addAll(dummyBodyBlock.getStatements());

        return container;
    }
}
